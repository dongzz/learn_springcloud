# 服务注册
zookeeper 代替 eureka

## 简介
zookeeper是一个分布式协调工具，可以实现注册中心功能

## 服务器
-   安装zookeeper服务器（docker安装，略）

## 客户端
-   pom
```xml
        <!--springboot整合zookeeper客户端-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
        </dependency>
```

-   yml
```yaml
spring:
  application:
    name: cloud-provider-payment
  cloud:
    zookeeper:
      connect-string: localhost:2181
```

-   java
```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
```

-   查看zookeeper状态
```
    echo stat | nc 127.0.0.1 2181
```