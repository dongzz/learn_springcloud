# 服务注册模块

## eureka-server
eureka 单节点
-   服务器
    
    -   pom
    
    ```xml
     <!--eureka server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    ```
    
    -   yml
    ```yaml
    eureka:
      instance:
        hostname: localhost #eureka服务端的实例名称
      client:
        #false表示不向注册中心注册自己
        register-with-eureka: false
        #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
        fetch-registry: false
        service-url:
          # 单节点
          #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址
          defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
    ```
    
    -   Application
    ```java
     @SpringBootApplication
     @EnableEurekaServer
     public class EurekaServerApplication {
    ```
-   客户端
    -   pom
    ```xml
    <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    ```
    -   yml
    ```yaml
    eureka:
      client:
        #表示是否将自己注册进EurekaServer默认为true
        register-with-eureka: true
        #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
        fetch-registry: true
        service-url:
          defaultZone: http://localhost:7001/eureka
      instance:
          # 修改主机名
          instance-id: payment-01
          # 访问路径可以显示ip地址
          prefer-ip-address: true
    ```
    -   java
    ```java
    @SpringBootApplication
    @EnableEurekaClient
    public class PaymentApplication {
    ```
    
    
## eureka-server
eureka 集群部署（互相注册，相互守望）
-   服务器
    -   节点1 yml
    ```yaml
    eureka:
      instance:
        hostname: localhost #eureka服务端的实例名称
      client:
        #false表示不向注册中心注册自己
        register-with-eureka: false
        #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
        fetch-registry: false
        service-url:
          #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址
          # 集群 相互注册
          defaultZone: http://www.dongz.com:7002/eureka/
    ```
    -   节点2 yml
    ```yaml
    eureka:
      instance:
    #    单节点
    #    hostname: localhost #eureka服务端的实例名称
    #    集群部署
        hostname: www.dongz.com #eureka服务端的实例名称
      client:
        #false表示不向注册中心注册自己
        register-with-eureka: false
        #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
        fetch-registry: false
        service-url:
          #设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址
          # 集群 相互注册
          defaultZone: http://localhost:7001/eureka/
    ```

-   客户端
    -   yml
    ```yaml
    eureka:
      client:
        #表示是否将自己注册进EurekaServer默认为true
        register-with-eureka: true
        #是否从EurekaServer抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true才能配合ribbon使用负载均衡
        fetch-registry: true
        service-url:
          defaultZone: http://localhost:7001/eureka,http://www.dongz.com:7002/eureka/
    ```
    
    ### eureka 服务发现
    -   Application新增注解
    ```java
        @EnableDiscoveryClient
    ```
    -   获取discoveryClient 对象
    ```java
        @Resource
        private DiscoveryClient discoveryClient;
    ```
    -   获取eureka服务对象列表
    ```java
    @GetMapping("/getDiscovery")
    public List<String> getDiscovery() {
        return discoveryClient.getServices();
    }
    ```
    -   获取服务对象实例信息
    ```java
    @GetMapping("/getServiceInfo/{name}")
    public List<ServiceInstance> getServiceInfo(@PathVariable String name) {
        return discoveryClient.getInstances(name);
    }
    ```
    