package com.ndkey.demo.overview.pojo;


import com.ndkey.demo.overview.client.NettyClient;
import com.ndkey.demo.overview.proxy.DynamicProxy;
import com.ndkey.demo.overview.service.HelloWorldService;
import org.junit.Test;

import java.lang.reflect.Proxy;

public class RpcRequestTest {


    @Test
    public void testDynamicProxy() {



        Class<?> helloWorldServiceClass = HelloWorldService.class;

        System.out.println();
        System.out.println();

        //创建代理类对象
        HelloWorldService so = (HelloWorldService) Proxy.newProxyInstance(helloWorldServiceClass.getClassLoader(),
                new Class[]{HelloWorldService.class}, new DynamicProxy());
        String result1 = so.helloWorld("zhangsan");
//        System.out.println("调用者收到的结果是:" + result1);
       /* System.out.println();
        System.out.println();
        String result2 = so.helloWorld("zhangsan");
        System.out.println("调用者收到的结果是" + result2);
        System.out.println();
        System.out.println();
        String result3 = so.helloWorld("zhangsan");
        System.out.println("调用者收到的结果是" + result3);
        System.out.println();
        System.out.println();
        String result4 = so.helloWorld("zhangsan");
        System.out.println("调用者收到的结果是" + result4);
        System.out.println();
        System.out.println();*/
//        System.out.println(result);



    }

    @Test
    public void test2() throws InterruptedException {
        new Thread(new NettyClient("127.0.0.1",8888)).start();
        new Thread(new NettyClient("127.0.0.1",8889)).start();

        Thread.sleep(3000);

        Class<?> helloWorldServiceClass = HelloWorldService.class;

        System.out.println();
        System.out.println();

        //创建代理类对象
        HelloWorldService so = (HelloWorldService) Proxy.newProxyInstance(helloWorldServiceClass.getClassLoader(),
                new Class[]{HelloWorldService.class}, new DynamicProxy());
        String result1 = so.helloWorld("zhangsan");
        System.out.println(result1);
        System.out.println("------------------");
        String result2 = so.helloWorld("zhangsan");
        System.out.println(result2);
        Thread.sleep(1000000L);
    }
}
