SpringMVC处理请求流程

首先我们来了解一下SpringMVC在处理http请求的整个流程中都在做些什么事。

SpringMVC请求处理流程

//图片来源于网络

从上图中我们可以总结springmvc的处理流程：

    客户端发送请求，被web容器(tomcat等)拦截到，web容器将请求交给DispatcherServlet。
    DispatcherServlet收到请求后将请求交给HandlerMapping(处理器映射器)查找该请求对应的Handler(处理器)。实际上这个过程在图上是分为两个的，这是因为一个请求url可能会有多个请求处理器，比如GET请求，POST请求等就是不同的处理器对象来处理的，所以需要一个HandlerAdapter(处理器适配器)来根据不同的请求参数来获取对应的处理器对象。
    获取到请求的处理器对象后，执行处理器的请求处理流程。这里的处理流程一般是值我们在开发中定义的业务流程。
    处理流程执行完毕后将返回的结果包装为一个ModelAndView对象返回给DispatcherServlet。
    DispatcherServlet通过ViewResolver(视图解析器)将ModelAndView解析为View。
    通过View渲染页面，响应给用户。

上面就是一个http请求从开始带完成响应中由SpringMVC完成的流程，我们的SpringMVC没有实际的那么复杂，不过相应的功能都会进行实现。
