package com.iflytek.mvc.servlet;

import com.iflytek.mvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther xiehuaxin
 * @create 2018-08-22 11:05
 * @todo
 */
public class DispatcherServlet extends HttpServlet {

    //用于存放扫描出来的所有全类名，如：com.cvc.nelson.controller.TestController.class,反射的时候需要用到
    List<String> classNames = new ArrayList<String>();
    //用于存放反射生成的bean，相当于IOC容器
    Map<String,Object> beans = new HashMap<String, Object>();
    //用于存放请求路径与方法的映射，例如请求接口http://39.108.66.150:8080/write-springmvc/test/query接口，会把它映射到对应的方法
    Map<String,Object> handlerMap = new HashMap<String, Object>();


    @Override
    public void init(ServletConfig config){

        //1.先扫描出包下的所有类名
        scanPackage("com.cvc");

        //2.根据扫描到的类名创建bean
        doInstance();

        //3.根据bean进行依赖注入
        doIoc();

        //4.请求路径与方法之间的映射
        buildUrlMapping();
    }

    /**
     * 建立请求与方法之间的对应关系
     * 例如我请求http://39.108.66.150:8080/write-springmvc/test/query 接口时，到底是执行哪个controller的哪个方法
     */
    private void buildUrlMapping() {
        if(beans.entrySet().size() <= 0) {
            System.out.println("没有一个实例化类......");
            return;
        }
        for(Map.Entry<String,Object> entity : beans.entrySet()) {
            Object obj = entity.getValue();
            Class<?> clazz = obj.getClass();
            //映射关系都在Controller中
            if(clazz.isAnnotationPresent(Controller.class)) {
                //类上的@RequestMapping
                RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
                String classPath = classMapping.value();
                /**
                 * 一个类可能有多个方法，每个方法上都可能有@RequestMapping
                 */
                Method[] methods = clazz.getMethods();
                for(Method method : methods) {
                    //有@RequestMapping注解的方法才处理
                    if(method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        String methodPath = methodMapping.value();
                        handlerMap.put(classPath + methodPath,method);
                    }else {
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 根据注解进行依赖注入
     */
    private void doIoc() {
        if(beans.entrySet().size() <= 0) {
            System.out.println("没有一个实例化类......");
            return;
        }
        for(Map.Entry<String,Object> entity : beans.entrySet()) {
            Object instance = entity.getValue();
            Class<?> clazz = instance.getClass();
            //因为这里没有用的mybatis，所以只会在controller中注入service
            if(clazz.isAnnotationPresent(Controller.class)) {
                //1.@Autowired注解是用在类的成员变量上的，所以要先拿到成员变量先
                Field[] fields = clazz.getDeclaredFields();
                for(Field field : fields) {
                    //2.成员变量里又可能包含一些非bean类型的成员变量，所以要判断有@Autowired注解的才要注入
                    if(field.isAnnotationPresent(Autowired.class)) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        //3.拿到上面定义的用于存放beans的Map的key，根据key去map中找到对应的实体
                        String key = autowired.value();
                        Object obj = beans.get(key);
                        //4.由于在被@Autowired修饰的成员变量一般是private的，所有要打开它的权限
                        field.setAccessible(true);
                        try {
                            //5.注入
                            field.set(instance,obj);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 为扫描到的类创建实例化对象
     */
    private void doInstance() {
        if(classNames.size() <= 0) {
            System.out.println("没有扫描到类.......");
            return;
        }
        for(String className : classNames) {
            try {
                String cn = className.replace(".class","");
                Class<?> clazz = Class.forName(cn);
                if(clazz.isAnnotationPresent(Controller.class)) {
                    //这一句要放在里面，我的包目录结构里，除了有类（class）,还有注解@Autowired等，否则会报错
                    Object instance = clazz.newInstance();
                    //这里注意不要写成Controller controller = clazz.getAnnotation(Controller.class),因为Controller的key是@RequestMapping的value
                    RequestMapping controllerRequestMapping = clazz.getAnnotation(RequestMapping.class);
                    beans.put(controllerRequestMapping.value(),instance);
                }else if (clazz.isAnnotationPresent(Service.class)) {
                    Object instance = clazz.newInstance();
                    Service service = clazz.getAnnotation(Service.class);
                    beans.put(service.value(),instance);
                }else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描basePackage包，获取到这个包以及这个包的子包中的所有类
     * @param basePackage
     */
    private void scanPackage(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.","/"));
//        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.","/"));
        String fileStr = url.getPath();
        File file = new File(fileStr);
        String[] files = file.list();
        for(String path : files) {
            File filePath = new File((fileStr + path));
            if(filePath.isDirectory()) {
                scanPackage(basePackage + "." + path);
            }else {
                classNames.add(basePackage + "." + filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.先获取到请求的路径,获取到的值例如：write-springmvc/test/query
        String uri = req.getRequestURI();
        /**
         * 2.截取，只留下Controller的@RequestMapping + 方法的@RequestMapping部分
         */
        //获取的的值如：write-springmvc
        String context = req.getContextPath();
        //只留下 /test/query
        String path = uri.replace(context,"");

        //3.然后以path作为key，去handlerMap中找到对应的方法对象
        Method method = (Method) handlerMap.get(path);

        //4.把参数封装到参数数组中(这一步在spingMVC中是以策略模式实现的，由于我不是很懂策略模式，所以这里用自己的方法达到相同的效果)
        Object[] args = hand(req,resp,method);

        //5.获取到Controller实例
        String pathStr = "/" + path.split("/")[1];
        Object instance = beans.get(pathStr);

        //6.调用方法
        try {
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把参数封装到参数数组中
     * @param request
     * @param response
     * @param method
     * @return
     */
    private static Object[] hand(HttpServletRequest request, HttpServletResponse response,Method method) {
        //拿到当前执行的方法有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        //根据参数的个数new一个参数数组，将方法里的所有参数赋值到args来
        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;
        for(Class<?> paramClazz : paramClazzs) {
            if(ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i ++] = request;
            }
            if(ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i ++] = response;
            }
            /**
             * 从0-3判断有没有RequestParam注解，很明显paramClazz为0和1时，不是，
             * 当为2和3时为@RequestParam,需要解析
             * [@com.enjoy.nelson.annotation.EnjoyRequestParam(value=name)]
             */
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if(paramAns.length > 0) {
                for(Annotation paramAn : paramAns) {
                    if(RequestParam.class.isAssignableFrom(paramAn.getClass())) {
                        RequestParam rp = (RequestParam) paramAn;
                        //找到注解里的name和age
                        args[args_i ++] = request.getParameter(rp.value());
                    }
                }
            }
            index ++;
        }
        return args;
    }
}
