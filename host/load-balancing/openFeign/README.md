# openFeign

## Feign 简介：
Feign 是一个声明式 WebService 客户端。使用 Feign 能让编写Web Service 客户端更加简单。

它的使用方法是定义一个服务接口然后在上面添加注解。Feign 也支持可插拔式的编码器和解码器。Spring Cloud 对 Feign 进行了封装，使其支持了Spring MVC标准注解和 HttpMessageConverters。Feign 可以与 Eureka和Ribbon 组合使用以支持负载均衡。

## Feign 能干什么？
Feign 旨在使编写Java Http 客户端变得更容易。
前面在使用 Ribbon+RestTemplate时，利用RestTemplate 对http请求的封装处理，形成了一套模板化的调用方法。但是在实际开发中，由于对服务依赖的调用可能不止一处，往往一个接口会被多处调用，所以通常都会针对每个微服务自行封装一些客户端类来包装这些依赖服务的调用。所以，Feign 在此基础上做了进一步封装，由他来帮助我们定义和实现依赖服务接口的定义。在Feign 的实现下，我们只需创建一个接口并使用注解的方式来配置它（以前是Dao接口上面标注Mapper注解，现在是一个微服务接口上面标注衣一个Feign注解即可），即可完成对服务提供方的接口绑定，简化了使用Spring Cloud Ribbon时，自动封装服务调用客户端的开发量。

Feign集成了Ribbon
利用Ribbon维护了Payment 的服务列表信息，并且通过轮询实现了客户端的负载均衡。而与Ribbon不同的是，通过feign 只需要定义服务绑定接口且以声明式的方法，优雅而简单的实现了服务调用。

## Feign 和 OpenFeign 两者区别
-   Feign是Springcloud组件中的一个轻量级Restful的HTTP服务客户端，Feign内置了Ribbon，用来做客户端负载均衡，去调用服务注册中心的服务。Feign的使用方式是：使用Feign的注解定义接口，调用这个接口，就可以调用服务注册中心的服务
```xml
   <dependency>
   	<groupId>org.springframework.cloud</groupId>
   	<artifactId>spring-cloud-starter-feign</artifactId>
   </dependency>
```

-   OpenFeign是springcloud在Feign的基础上支持了SpringMVC的注解，如@RequestMapping等等。OpenFeign的@FeignClient可以解析SpringMVC的@RequestMapping注解下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。
```xml
   <dependency>
   	<groupId>org.springframework.cloud</groupId>
   	<artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
```

## 客户端
-   pom
```xml
        <!--openfeign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```
-   Application
    -   @EnableFeignClients //开启Feign
```java
package com.dongz.springcloud;

import com.dongz.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author dong
 * @date 2020/4/29 15:12
 * @desc
 */
@SpringBootApplication
//@EnableEurekaClient
@EnableDiscoveryClient
//@RibbonClient(name = "CLOUD-PROVIDER-SERVICE",configuration = MySelfRule.class)
@EnableFeignClients //开启Feign
public class ConsumerOrderController {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerOrderController.class, args);
    }
}
```
-   业务类
    -   @FeignClient(value = "CLOUD-PROVIDER-SERVICE")  //指定调用哪个微服务
```java
package com.dongz.springcloud.services;

import com.dongz.springcloud.entities.Payment;
import com.dongz.springcloud.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author dong
 * @date 2020/5/4 10:03
 * @desc
 */
@Component
@FeignClient(value = "payment-service")  //指定调用哪个微服务
public interface paymentService {
    @GetMapping("/api/pay/getById/{id}")
    Result<Payment> getById(@PathVariable Long id) ;
}
```    
```java
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    PaymentService service;

    @GetMapping("/getById2/{id}")
    public Result<Payment> getById2(@PathVariable("id") Long id) {
        return service.getById(id);
    }
}
```

## 超时
 openFeign默认超时等待1秒
```yaml
#设置feign 客户端超时时间（openFeign默认支持ribbon）
ribbon:
  #指的是建立连接所用的时间，适用于网络状况正常的情况下，两端连接所用的时间
  ReadTimeout: 5000
  #指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeout: 5000
```

## 日志

### 日志功能：
Feign 提供了日志打印功能，可以通过配置来调整日志级别，从而了解 Feign 中 Http 请求的细节。
说白了就是对接口的调用情况进行监控和输出

### 日志级别：
-   NONE：默认的，不显示任何日志
-   BASIC：仅记录请求方法、URL、响应状态码及执行时间
-   HEADERS：除了 BASIC 中定义的信息之外，还有请求和响应的头信息
-   FULL：除了 HEADERS 中定义的信息之外，还有请求和响应的正文及元数据

### 客户端
-   日志Bean
```java
@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
```
-   yml
```yaml
logging:
  level:
    #feign日志以什么级别监控哪个接口
    com.dongz.springcloud.services.PaymentService: debug
```