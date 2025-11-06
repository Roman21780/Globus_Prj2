package com.example.prj2;

import org.springframework.boot.SpringApplication;

public class TestPrj2Application {

    public static void main(String[] args) {
        SpringApplication.from(Prj2Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
