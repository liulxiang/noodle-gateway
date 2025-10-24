package com.noodle.app.collect.storage.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.noodle.app.collect.protocol.config.DataStorageConfig;
import com.noodle.app.collect.storage.DataStorage;
import com.noodle.app.collect.storage.model.ProtocolData;

/**
 * InfluxDB数据存储实现
 */
@Component
@ConditionalOnProperty(name = "data.storage.type", havingValue = "influxdb")
public class InfluxDataStorage implements DataStorage {
	 private final String TAG = "tag";
	    private final String FIELD_VALUE = "value";
    private static final Logger logger = LoggerFactory.getLogger(InfluxDataStorage.class);

    @Autowired
    private DataStorageConfig storageConfig;

    private InfluxDBClient influxDBClient;
    DataStorageConfig.InfluxConfig config;
    @PostConstruct
    @Override
    public void initialize() {
        logger.info("Initializing InfluxDB data storage...");
        try {
            DataStorageConfig.InfluxConfig config = storageConfig.getInflux();
            influxDBClient = InfluxDBClientFactory.create(
                    config.getUrl(), 
                    config.getToken().toCharArray(), 
                    config.getOrg(), 
                    config.getBucket()
            );
            this.config=config;
            if (isConnected()) {
                logger.info("InfluxDB data storage initialized successfully");
            } else {
                logger.error("Failed to connect to InfluxDB");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize InfluxDB: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    @Override
    public void destroy() {
        logger.info("Destroying InfluxDB data storage...");
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }

    @Override
    public void store(ProtocolData data) {
        try {
        	List<Point> points = createSingleTagPoint(data);
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoints(points);
            
            logger.info("Stored data to InfluxDB: {}", data);
            
        } catch (Exception e) {
            logger.error("Failed to store data to InfluxDB: {}", e.getMessage(), e);
        }
    }

    @Override
    public void storeBatch(List<ProtocolData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        logger.info("===========dataList:"+dataList.size()+"============"+JSON.toJSONString(dataList));
        try {
            List<Point> points = new ArrayList<>();
            for (ProtocolData data : dataList) {
                points.addAll(createSingleTagPoint(data));
            }
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
           if(points.size()>0) writeApi.writePoints(points);
            logger.info("Stored {} data points to InfluxDB in batch", dataList.size());
            
        } catch (Exception e) {
            logger.error("Failed to store batch data to InfluxDB: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getStorageType() {
        return "influxdb";
    }

    @Override
    public boolean isConnected() {
        try {
            if (influxDBClient != null) {
                // 尝试执行一个简单的查询来测试连接
                String flux = "buckets() |> limit(n:1)";
                QueryApi queryApi = influxDBClient.getQueryApi();
                List<FluxTable> tables = queryApi.query(flux);
                return tables != null;
            }
            return false;
        } catch (Exception e) {
            logger.error("InfluxDB connection test failed: {}", e.getMessage());
            return false;
        }
    }
    /**
     * 创建InfluxDB Point
     */
    private  List<Point> createSingleTagPoint(ProtocolData data) {
    	 List<Point> points = new ArrayList<>();
         if(data.getOrgData()==null) {
            logger.info("===========数据为空=======");
    		return new  ArrayList<Point>();
    	 }
        if (!JSON.isValid(data.getOrgData().toString())) {
            logger.info("===========数据不是JSON格式=======");
            return new ArrayList<Point>();
        }
    	 JsonObject allObject = JsonParser.parseString(data.getOrgData().toString()).getAsJsonObject();
    	 if(allObject.get("deviceId")==null) {
    		return new  ArrayList<Point>();
    	 }
    	 String deviceId= allObject.get("deviceId").getAsString();
    	 JsonElement pointDataStr= allObject.get("data");
    	 JsonObject jsonObject = pointDataStr.getAsJsonObject();
    	for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key =deviceId+"_"+entry.getKey();
            logger.info("===========key:"+key);
            JsonElement value = entry.getValue();
            logger.info("===========value:"+value);
            if (value == null || value.isJsonNull()) {
                System.out.println("值为 null");
                continue;
            }
            Point point = Point
                    .measurement(this.config.getTable())
                    .addTag(TAG, key)
                    .time(Instant.now(), WritePrecision.S);
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                	try {
                        // 尝试获取数字值
                        double doubleValue = primitive.getAsDouble();
                        System.out.println("数字值: " + doubleValue);
                        // 判断是否是整数
                        if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
                            System.out.println("这是一个整数");
                            long longValue = primitive.getAsLong();
                            point.addField(FIELD_VALUE, new Double(longValue));
                        } else {
                            System.out.println("这是一个浮点数");
                            point.addField(FIELD_VALUE, doubleValue);
                        }
                        points.add(point);
                    } catch (NumberFormatException e) {
                        System.out.println("数字格式错误");
                    }
               } else if (primitive.isString()) {
                    String stringValue = primitive.getAsString();
                    System.out.println("字符串值: " + stringValue);
               }
          }
    	}
    	 logger.info("===========points:"+points.size()+"============"+JSON.toJSONString(points));
    	 return points;
    }

}