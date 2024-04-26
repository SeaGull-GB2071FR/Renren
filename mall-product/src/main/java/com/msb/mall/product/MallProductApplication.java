package com.msb.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;


@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.msb.mall.product.feign")
// 放开注册中心
@EnableDiscoveryClient
@SpringBootApplication
// 指定Mapper接口对应的路径
@MapperScan("com.msb.mall.product.dao")
//@ComponentScan(basePackages = "com.msb")
public class MallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }


}
