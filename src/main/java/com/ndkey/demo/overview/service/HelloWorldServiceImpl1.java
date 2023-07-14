package com.ndkey.demo.overview.service;

public class HelloWorldServiceImpl1 implements HelloWorldService {
    @Override
    public String helloWorld(String name) {
        return "Hello World1" + name;
    }
}
