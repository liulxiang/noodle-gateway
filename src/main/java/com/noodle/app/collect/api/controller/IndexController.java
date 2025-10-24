package com.noodle.app.collect.api.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class IndexController {
    @GetMapping(value= {"/","/index.html","/index"})
	public String execute(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		// 当前sessionId
		model.addAttribute("sessionId", request.getSession().getId());
		// 单点退出地址
		model.addAttribute("urlName","index");
		 return "index";
	}
    @GetMapping(value= {"/404","/404.html"})
    	public String to404(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
    		return "404";
    	}
    @GetMapping("/heartbeat")
    @ResponseBody
    public Boolean heartbeat()throws ParseException{
          return true;
    } 
}