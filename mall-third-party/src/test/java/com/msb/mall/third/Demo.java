package com.msb.mall.third;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.SetBucketCORSRequest;
import java.util.ArrayList;

public class Demo {

    public static void main(String[] args) throws Exception {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "examplebucket";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            SetBucketCORSRequest request = new SetBucketCORSRequest(bucketName);

            // 跨域资源共享规则的容器，每个存储空间最多允许10条规则。
            ArrayList<SetBucketCORSRequest.CORSRule> putCorsRules = new ArrayList<SetBucketCORSRequest.CORSRule>();

            SetBucketCORSRequest.CORSRule corRule = new SetBucketCORSRequest.CORSRule();

            ArrayList<String> allowedOrigin = new ArrayList<String>();
            // 指定允许跨域请求的来源。
            allowedOrigin.add( "http://example.com");

            ArrayList<String> allowedMethod = new ArrayList<String>();
            // 指定允许的跨域请求方法(GET/PUT/DELETE/POST/HEAD)。
            allowedMethod.add("GET");

            ArrayList<String> allowedHeader = new ArrayList<String>();
            // 是否允许预取指令（OPTIONS）中Access-Control-Request-Headers头中指定的Header。
            allowedHeader.add("x-oss-test");

            ArrayList<String> exposedHeader = new ArrayList<String>();
            // 指定允许用户从应用程序中访问的响应头。
            exposedHeader.add("x-oss-test1");
            // AllowedOrigins和AllowedMethods最多支持一个星号（*）通配符。星号（*）表示允许所有的域来源或者操作。
            corRule.setAllowedMethods(allowedMethod);
            corRule.setAllowedOrigins(allowedOrigin);
            // AllowedHeaders和ExposeHeaders不支持通配符。
            corRule.setAllowedHeaders(allowedHeader);
            corRule.setExposeHeaders(exposedHeader);
            // 指定浏览器对特定资源的预取（OPTIONS）请求返回结果的缓存时间，单位为秒。
            corRule.setMaxAgeSeconds(10);

            // 最多允许10条规则。
            putCorsRules.add(corRule);
            // 已存在的规则将被覆盖。
            request.setCorsRules(putCorsRules);
            // 指定是否返回Vary: Origin头。指定为TRUE，表示不管发送的是否为跨域请求或跨域请求是否成功，均会返回Vary: Origin头。指定为False，表示任何情况下都不会返回Vary: Origin头。
            // request.setResponseVary(Boolean.TRUE);
            ossClient.setBucketCORS(request);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}