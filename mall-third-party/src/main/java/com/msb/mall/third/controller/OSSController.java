package com.msb.mall.third.controller;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.aliyun.oss.model.SetBucketCORSRequest;
import com.msb.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:8001")
@RestController
public class OSSController {

    @Autowired
    private OSS ossClient;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint; // 地域
    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;  // bucket
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;  //  alibaba AccessId

    /**
     * 服务端生成签名
     *
     * @return 返回信息通过R对象来返回
     */

    @RequestMapping("/oss/policy")
    public R getOssPolicy() {

        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = format + "/"; // 用户上传文件时指定的前缀。

        // 创建OSSClient实例。
        //OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        Map<String, String> respMap = null;
        try {
            long expireTime = 30;  // 过期时长
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000; // ms  过期时间
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);  // 大小要求
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir); // 前缀要求

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);  //  Policy为经过Base64编码过的字符串
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));
//
//            //跨域问题
//            try {
//                SetBucketCORSRequest request = new SetBucketCORSRequest(bucket);
//
//                // 跨域资源共享规则的容器，每个存储空间最多允许10条规则。
//                ArrayList<SetBucketCORSRequest.CORSRule> putCorsRules = new ArrayList<SetBucketCORSRequest.CORSRule>();
//
//                SetBucketCORSRequest.CORSRule corRule = new SetBucketCORSRequest.CORSRule();
//
//                ArrayList<String> allowedOrigin = new ArrayList<String>();
//                // 指定允许跨域请求的来源。
//                allowedOrigin.add("*");
//
//                ArrayList<String> allowedMethod = new ArrayList<String>();
//                // 指定允许的跨域请求方法(GET/PUT/DELETE/POST/HEAD)。
//                allowedMethod.add("GET");
//                allowedMethod.add("PUT");
//                allowedMethod.add("DELETE");
//                allowedMethod.add("POST");
//                allowedMethod.add("HEAD");
//
//                ArrayList<String> allowedHeader = new ArrayList<String>();
//                // 是否允许预取指令（OPTIONS）中Access-Control-Request-Headers头中指定的Header。
//                allowedHeader.add("*");
//
////                ArrayList<String> exposedHeader = new ArrayList<String>();
////                // 指定允许用户从应用程序中访问的响应头。
////                exposedHeader.add("x-oss-test1");
//                // AllowedOrigins和AllowedMethods最多支持一个星号（*）通配符。星号（*）表示允许所有的域来源或者操作。
//                corRule.setAllowedMethods(allowedMethod);
//                corRule.setAllowedOrigins(allowedOrigin);
//                // AllowedHeaders和ExposeHeaders不支持通配符。
//                corRule.setAllowedHeaders(allowedHeader);
////                corRule.setExposeHeaders(exposedHeader);
//                // 指定浏览器对特定资源的预取（OPTIONS）请求返回结果的缓存时间，单位为秒。
//                corRule.setMaxAgeSeconds(10);
//
//                // 最多允许10条规则。
//                putCorsRules.add(corRule);
//                // 已存在的规则将被覆盖。
//                request.setCorsRules(putCorsRules);
//                // 指定是否返回Vary: Origin头。指定为TRUE，表示不管发送的是否为跨域请求或跨域请求是否成功，均会返回Vary: Origin头。指定为False，表示任何情况下都不会返回Vary: Origin头。
//                // request.setResponseVary(Boolean.TRUE);
//                ossClient.setBucketCORS(request);
//            } catch (OSSException oe) {
//                System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                        + "but was rejected with an error response for some reason.");
//                System.out.println("Error Message:" + oe.getErrorMessage());
//                System.out.println("Error Code:" + oe.getErrorCode());
//                System.out.println("Request ID:" + oe.getRequestId());
//                System.out.println("Host ID:" + oe.getHostId());
//            } catch (ClientException ce) {
//                System.out.println("Caught an ClientException, which means the client encountered "
//                        + "a serious internal problem while trying to communicate with OSS, "
//                        + "such as not being able to access the network.");
//                System.out.println("Error Message:" + ce.getMessage());
//            } finally {
//                if (ossClient != null) {
//                    ossClient.shutdown();
//                }
//            }


        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return R.ok().put("data", respMap);
    }
}
