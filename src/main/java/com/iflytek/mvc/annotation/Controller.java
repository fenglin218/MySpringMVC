package com.iflytek.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.*;


//因为Controller一般作用在类上，这里的ElementType.TYPE的意思是这个注解用在类上
@Target(ElementType.TYPE)
@Documented
//在运行时可以通过反射获取相应的注解
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    String value() default "";
}
