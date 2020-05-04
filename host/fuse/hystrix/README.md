# 熔断器

## Hystrix 简介
Hystrix是一个用于处理分布式系统的延迟和容错的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等，Hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，已提高分布式系统的弹性。
“ 断路器 ” 本身是一种开关装置，当某个服务单元发送故障之后，通过断路器的故障监控（类似熔断保险丝），向调用方返回一个符合预期的、可处理的备选响应（FallBack），而不是长时间的等待或抛出调用方无法处理的异常，这样就保证了服务调用方的线程不会被长时间、不必要地占用，从而避免了故障在分布式系统中的蔓延，乃至雪崩。

## Hystrix 功能：
-   服务降级
-   服务熔断
-   接近实时的监控
-   限流、隔离等

## Hystrix重要概念：
   
-   1.服务降级 fallback
   服务器忙，请稍后再试，不让客户端等待并立刻返回一个友好提示，fallback
   哪些情况会发出降级？
   -    程序运行异常
   -    超时
   -    服务熔断触发服务降级
   -    线程池 / 信号量打满也会导致服务降级
   
-   2.服务熔断 break
   类似保险丝达到最大服务访问后，直接拒绝访问，拉闸限电，然后调用服务降级的方法并返回友好提示
   
-   3.服务限流 flowlimit
   秒杀高并发等操作，严禁一窝蜂的过来拥挤，大家排队，一秒钟N个，有序进行
   
## 服务降级

### 降级容错解决的维度要求
-   超时导致服务器变慢 (转圈)
    -   超时不再等待
-   出错（宕机或程序运行出错）
    -   出错要有兜底
#### 解决
-    对方服务(8001)超时了，调用者(80)不能一直卡死等待，必须有服务降级
-    对方服务(8001)宕机了，调用者(80)不能一直卡死等待，必须有服务降级
-    对方服务(8001)OK，调用者(80)自己出故障或有自我要求(自己的等待时间小于服务提供者)，自己处理降级
    
设置自身调用超时时间的峰值，峰值内可以正常运行，超过了需要有兜底的方法处理，作服务降级fallback
服务降级 fallback 既可以放在服务端，也可以放在客户端，但是我们一般放在客户端，这里两种都演示一下。

### (1) 服务提供者服务降级
-   pom
```xml
        <!--hystrix-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```
-   业务类
```java
public class PaymentController {
    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("线程池，线程ID：" + Thread.currentThread().getId() + ",线程name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }
    @GetMapping("/getById2/{id}")
    @HystrixCommand(fallbackMethod = "getById",commandProperties = {
            //设置这个线程的超时时间是3s，3s内是正常的业务逻辑，超过3s调用fallbackMethod指定的方法进行处理
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public Result<Payment> getById2(@PathVariable Long id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("线程池，线程ID：" + Thread.currentThread().getId() + ",线程name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }
}
```
-   application
    -   @EnableCircuitBreaker 激活
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
```
### (2) 消费者服务降级
-   pom
    同上
-   yml
```yaml
#yml添加配置,开启 hystrix
feign:
 hystrix:
   enabled: true
```
-   application
    -   @EnableHystrix
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients //开启Feign
@EnableHystrix
public class ConsumerOrderController {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerOrderController.class, args);
    }
}

```
-   业务类
```java
public class OrderController {

    @Resource
    PaymentService service;

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "/api/pay/getById/" + id, Result.class);
    }

    @HystrixCommand(fallbackMethod = "getById",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    @GetMapping("/getById2/{id}")
    public Result<Payment> getById2(@PathVariable("id") Long id) {
        return service.getById2(id);
    }
}
```
### 问题1：
这样如果每个业务方法都对应一个兜底的方法，100个方法就有100个服务降级，会出现代码膨胀问题，我们需要一个统一的 fallbackMethod，统一的和自定义的分开。

#### 解决问题：
-   @DefaultProperties(defaultFallback = "")
    -   1 : 1 每个方法配置一个服务降级方法，造成代码膨胀
    -   1 : N 除了个别重要核心业务有专属，其他普通的可以通过@DefaultProperties(defaultFallback = “”) 统一跳转到统一处理结果页面
这样通用的和独享的各自分开，避免了代码膨胀，合理减少了代码量。

#### (1) 服务提供者服务降级
-   业务类
    -   @DefaultProperties(defaultFallback = "getById") // 设置全局fallback方法
    -   @HystrixCommand
```java
@RestController
@RequestMapping("/api/pay")
@Slf4j
@DefaultProperties(defaultFallback = "getById")
public class PaymentController {

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("method 1； 线程池，线程池ID：" + Thread.currentThread().getId() + ",线程池name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }

    @GetMapping("/getById2/{id}")
    @HystrixCommand
    public Result<Payment> getById2(@PathVariable Long id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return new Result<>(444, "查询失败");
        }
        log.info("method 2；线程池，线程池ID：" + Thread.currentThread().getId() + ",线程池name：" + Thread.currentThread().getName());
        return new Result<>(200, "查询成功", payment);
    }
}
```

### 问题2：
现在客户端与服务端关系紧紧耦合，客户端能跑是因为接口调用了微服务的业务逻辑方法，我们如果针对客户端接口做一些处理，把它调用的所有微服务方法进行降级，就可以解决耦合问题。

#### 解决问题：
这个案例服务降级处理是在客户端80完成的，与服务端8001没有关系，只需要为 Feign 客户端定义的接口添加一个服务降级处理的实现类即可实现解耦。

#### (2) 消费者服务降级
-   业务类
    -   使用@FeignClient(fallback = xxx.class)指定哪个类来处理异常
```java
@Component
@FeignClient(value = "payment-service", fallback = PaymentServiceFallback.class)  //指定调用哪个微服务
public interface PaymentService {
    @GetMapping("/api/pay/getById/{id}")
    Result<Payment> getById(@PathVariable("id") Long id) ;

    @GetMapping("/api/pay/getById2/{id}")
    Result<Payment> getById2(@PathVariable("id") Long id) ;
}

```
-   全局处理类
```java
@Component
@Slf4j
public class PaymentServiceFallback implements PaymentService {

    @Override
    public Result<Payment> getById(Long id) {
        log.error("访问异常，getById");
        return null;
    }

    @Override
    public Result<Payment> getById2(Long id) {
        log.error("访问异常，getById2");
        return null;
    }
}
```

## 熔断机制概述：
   
   熔断机制是应对雪崩效应的一种微服务链路保护机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回错误的响应信息。
   当检测到该节点微服务调用响应正常后，恢复调用链路。
   
   在SpringCloud框架里，熔断机制通过Hystrix实现，Hystrix会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败，就会启动熔断机制。熔断机制的注解是@HystrixCommand

### 服务的降级 --> 进而熔断 --> 恢复调用链路

### 总结：
-   熔断类型
    -   熔断打开
        请求不再进行调用当前服务，内部设置时钟一般为MTTR（平均故障处理时间），当打开时长达到所设时钟则进入半熔断状态
    -   熔断关闭
        熔断关闭不会对服务进行调用
    -   熔断半开
        部分请求根据规则调用当前服务，如果请求成功且符合规则则认为当前服务恢复正常，关闭熔断
-   断路器在什么情况下开始起作用
    设计到断路器的三个重要参数：快照时间窗、请求总数阈值、错误百分比阈值
    -   1.快照时间窗：断路器确定是否打开需要统计一些请求和错误数据，而统计的时间范围就是快照时间窗，默认为最近的10秒
    -   2.请求总数阈值：在快照时间内，必须满足请求总数阈值才有资格熔断。默认为20，意味着在10秒内，如果该hystrix命令的调用次数不足20次，即使所有的请求都超时或其他原因失败，断路器都不会打开
    -   3.错误百分比阈值：当请求总数在快照时间窗内超过阈值，比如发生了30次调用，如果在这30次调用中，有15次发生了超时异常，也就是超过50%的错误百分比，在默认设定50%阈值情况下，这时候就会将断路器打开

-   断路器开启或关闭的条件
    -   1.当满足一定的阈值的时候（默认10秒内超过20个请求次数）
    -   2.当失败率达到一定的时候（默认10秒内超过50%的请求失败）
    -   3.到达以上阈值，断路器将会开启
    -   4.当开启的时候，所有请求都不会进行转发
    -   5.一段时间后（默认是5秒），这个时候断路器是半开状态，会让其中一个请求进行转发。如果成功，断路器会关闭，若失败，继续开启。重复4和5。

-   断路器打开之后
    -   1.再有请求调用的时候，将不会调用主逻辑，而是直接调用降级fallback。通过断路器，实现了自动地发现错误并将降级逻辑切换为主逻辑，减少响应延迟的效果。
    -   2.原来的主逻辑要如何恢复呢？
对于这一问题，hystrix也为我们实现了自动恢复功能
当断路器打开，对主逻辑进行熔断之后，hystrix会启动一个休眠时间窗，在这个时间窗内，降级逻辑是临时的成为主逻辑，当休眠时间窗到期，熔断器将进入半开状态，释放一次请求到原来的主逻辑上，如果此次请求正常返回，那么断路器将继续闭合，主逻辑恢复，如果这次请求依然有问题，断路器继续进入打开状态，休眠时间窗重新计时。

#### 案例
-   HystrixCommandProperties.class
##### 服务端
```java
@RestController
@RequestMapping("/api/pay")
@Slf4j
public class PaymentController {
    // ==== 服务熔断
    @GetMapping("/paymentCircuitBreaker/{id}")
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),//是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),//请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),//时间窗口期（时间范围）
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),//失败率达到多少后跳闸
    })
    public String paymentCircuitBreaker(@PathVariable("id") Long id) {

        if (id < 0) {
            throw new RuntimeException("id 不能为负");
        }
        return "线程池：" + Thread.currentThread().getName() + "流水号" + IdUtil.simpleUUID();
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id") Long id) {
        return "id不能为负，线程池：" + Thread.currentThread().getName() + "流水号" + IdUtil.simpleUUID();
    }
}
```

## Hystrix 图形化Dashboard搭建及监控测试
除了隔离依赖服务的调用意外，Hystrix还提供了准实时的调用监控（Hystrix Dashboard），Hystrix会持续的记录所有通过Hystrix发起的请求的执行信息，并以统计报表和图形的形式展示给用户，包括每秒执行多少请求多少成功，多少失败等。Netflix 通过 hystrix-metrics-event-stream项目实现了对以上指标的监控。SpringCloud也提供了Hystrix Dashboard的整合，对监控内容转化成可视化界面。

### 搭建
-   pom
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>fuse</artifactId>
        <groupId>org.dongz.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>dashboard</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```
-   application
    -   @EnableHystrixDashboard
```java
@SpringBootApplication
@EnableHystrixDashboard
public class DashBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashBoardApplication.class, args);
}
```
-   client application
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    //主启动类添加代码
    /**
     * 此配置是为了服务监控而配置，与服务容错本身无关,SpringCloud升级后的坑
     * ServletRegistrationBean因为springboot的默认路径不是"/hystrix.stream"，
     * 只要在自己的项目里配置上下面的servlet就可以了
     */
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```