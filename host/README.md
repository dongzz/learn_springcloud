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


## load-balancing
负载均衡