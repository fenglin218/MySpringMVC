package com.iflytek.mvc.controller;

import com.iflytek.mvc.annotation.Autowired;
import com.iflytek.mvc.annotation.Controller;
import com.iflytek.mvc.annotation.RequestMapping;
import com.iflytek.mvc.annotation.RequestParam;
import com.iflytek.mvc.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired("TestServiceImpl")
    TestService testService;

    @RequestMapping("/query")
    public void queryData(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam("userName") String userName,
                          @RequestParam("age") String age){

        String result = testService.query(userName,age);
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }

    }

}
