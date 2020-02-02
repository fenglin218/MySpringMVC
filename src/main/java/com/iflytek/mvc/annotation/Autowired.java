package com.iflytek.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.*;

//因为Autowired一般是用在类的成员变量上，这里的ElementType.FIELD的意思是这个注解用在类的成员变量上
@Target(ElementType.FIELD)
//表示在运行时可以通过反射获取相应的注解
@Retention(RetentionPolicy.RUNTIME)
//表示：当我们的注解包含在javadoc中，在public @interface EnjoyAutowired{}上声明@Inherited，EnjoyAutowired注解就可以被继承
@Documented
public @interface Autowired {
    //@Autowired有时候会带参数，例如@Autowired("orderService"),下面这一行的意思是，在使用EnjoyAutowired注解时，可以在后面带参数
    String value() default "";
}
