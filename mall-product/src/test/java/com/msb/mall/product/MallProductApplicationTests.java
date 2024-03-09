package com.msb.mall.product;


import com.aliyun.oss.OSSClient;
import com.msb.mall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@SpringBootTest
public class MallProductApplicationTests {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OSSClient ossClient;

    @Test
    public void testStringRedisTemplate() {
        // 获取操作String类型的Options对象
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 插入数据
        ops.set("name","bobo"+ UUID.randomUUID());
        // 获取存储的信息
        System.out.println("刚刚保存的值："+ops.get("name"));
    }

    @Test
    public void testUploadFile() throws FileNotFoundException {
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-guangzhou.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5tQc3FY99jkxVC6nVUAT";
//        String accessKeySecret = "SpyvsWAFPkxtozIYTDdT7cMazjhGKE";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\阿爽\\Desktop\\avatar.jpg");
        // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("gb2071fr", "422.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("长传图片成功...");
    }

//    @Test
//    public void TestRedisson() {
//        Config config = new Config();
//        //config.useSingleServer().setAddress("redis://127.0.0.1:6379").setPassword("123456");
//        config.useSingleServer().setAddress("redis://192.168.44.135:6379");
//        RedissonClient redissonClient = Redisson.create(config);
//
//        RBucket<Object> bucket = redissonClient.getBucket("name");
//        //设置值为victory，过期时间为3小时
//        bucket.set("victory",30, TimeUnit.HOURS);
//        Object value = bucket.get();
//        System.out.println(value);
//        //通过key取value值
//        Object name = redissonClient.getBucket("name").get();
//        System.out.println(name);
//
//        //====================关闭客户端====================
//        redissonClient.shutdown();
//
//    }

}
