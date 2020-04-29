# host
父工程

## payment
支付模块

## consumer-order
订单模块

### RestTemplate(模拟http请求进行远程调用 @RequestBody)
```java
@Configuration
public class ApplicationContextConfig {

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
```
-   @LoadBalanced 负载均衡注解配置

## api-commons
公用工程

## service-registration
服务注册
