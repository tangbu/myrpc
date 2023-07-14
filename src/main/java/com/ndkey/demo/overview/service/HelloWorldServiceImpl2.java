package com.ndkey.demo.overview.service;

public class HelloWorldServiceImpl2 implements HelloWorldService {
    @Override
    public String helloWorld(String name) {
        return "Hello World2" + name;
    }
}
