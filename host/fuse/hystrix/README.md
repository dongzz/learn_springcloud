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
