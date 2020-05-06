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
-   eureka (停更)
-   zookeeper
-   consul

### 三个服务注册中心的异同

| 组件名 | 语言 | CAP  | 服务监控检查 | 对外暴露接口 | SpringCloud继承 |
| ------ | ---- | ---- | ------------ | ------------ | --------------- |
|  Eureka|  JAVA|  AP  |     可配支持   |      HTTP    |  已集成  |
|  Consul|   GO |  CP  |     支持      |   HTTP/DNS   |   已集成 |
|Zookeeper| JAVA|  CP  |     支持      |     客户端    |   已集成 |

#### CAP
-   C：Consistency （强一致性）
-   A：Available （可用性）
-   P：Partition tolerance （分区容错性）

#### CAP理论的核心
```
一个分布式系统不可能同时很好的满足一致性，可用性和分区容错性这三个需求。
因此，根据CAP原理将NoSQL数据库分成了满足CA原则、
满足CP原则和满足AP原则三大类。

-   CA - 单点集群，满足一致性，可用性的系统，通常在可扩展性上不太强
    -   RDBMS
-   CP - 满足一致性，分区容错性的系统，通常性能不是特别高
    -   MongoDB
    -   HBase
    -   Redis
-   AP - 满足可用性，分区容错性的系统，通常可能对一致性要求低一些
    -   CouchDB
    -   Cassandra
    -   DynamoDB
    -   Riak

Zookeeper、Consul注册的微服务是一个临时节点，只要微服务不可用，发心跳测试收不到了，就迅速剔除微服务，微服务恢复过来以后，会重新换一个serviceID。
```

## load-balancing
负载均衡
-   ribbon
-   openFeign

## fuse
熔断器
-   Hystrix

### 分布式系统面临的问题：
```
复杂分布式体系结构中的应用程序有数十个依赖关系，每个依赖关系在某个时候将不可避免的失败。
```
### 服务雪崩
```
多个微服务之间调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其他的微服务，这就是所谓的 “ 扇出 ” 。
如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A 的调用就会占用越来越多的系统资源，进而引起系统崩溃，所谓的 “ 雪崩效应 ”。

对于高流量的应用来说，单一的后端依赖可能会导致所有服务器上的所有资源都在几秒钟内饱和。比失败更糟糕的是，这些应用程序还可能导致服务之间的延迟增加，备份队列，线程和其他系统资源紧张，导致整个系统发生更多的级联故障。这些都表示需要对故障和延迟进行隔离和管理，以便单个依赖关系的失败，不能取消整个应用程序或系统。

所以，通常当你发现一个模块下的某个实例失败后，这时候这个模块依然还会接受流量，然后这个有问题的模块还调用了其他的模块，这样就会发生级联故障，或者叫 雪崩。

要避免这样的级联故障，就需要有一种链路中断的方案：
服务降级、服务熔断
```
## service-gateway
服务网关
