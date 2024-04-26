package com.msb.mall.cart;

import com.msb.common.constant.SMSConstant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class MallCartApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void test() {
        String code = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + "13536926109");
        System.out.println(code);
    }
}
