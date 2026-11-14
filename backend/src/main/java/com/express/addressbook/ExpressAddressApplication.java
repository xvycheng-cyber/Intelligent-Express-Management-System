package com.express.addressbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan(basePackages = "com.express.addressbook.servlet")
public class ExpressAddressApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpressAddressApplication.class, args);
    }
}
