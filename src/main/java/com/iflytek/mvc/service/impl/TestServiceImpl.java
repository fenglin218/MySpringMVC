package com.iflytek.mvc.service.impl;

import com.iflytek.mvc.annotation.Service;
import com.iflytek.mvc.service.TestService;

@Service("TestServiceImpl")
public class TestServiceImpl implements TestService {

    public String query(String userName, String age) {
        return "模拟到数据库查询到数据： user_name = " + userName + "     age = " + age;
    }
}