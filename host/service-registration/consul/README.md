# 服务注册模块
consul 替代eureka

## 简介
Consul是一套开源的分布式服务发现和配置管理系统，由HashiCorp公司用Go语言开发。

提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建全方位的服务网络，总之Consul提供了一种完整的服务网格解决方案。

它具有很多优点。包括：基于 raft 协议，比较简洁；支持健康检查，同时支持 HTTP 和 DNS 协议 支持跨数据中心的 WAN 集群 提供图形界面 跨平台，支持Linux、Mac、Windows

## Consul功能：
-   服务发现：提供 HTTP 和 DNS 两种发现方式
-   健康监测：支持多种方式，HTTP、TCP、Docker、Shell脚本定制化
-   KV存储：Key、Value的存储方式
-   多数据中心：Consul支持多数据中心
-   可视化界面  http://localhost:8500

## docker 
```
docker run -d -p 8500:8500 -v /Users/dong/opt/consul:/consul/data -e CONSUL_BIND_INTERFACE='eth0' --name=consul1 consul agent -server -bootstrap -ui -client='0.0.0.0'
```

### 客户端
-   pom
```xml
        <!--springcloud consul-server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
```
-   yml
```yaml
spring:
  application:
    name: consul-provider-peyment
###consul注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        #hostname: 127.0.0.1
        service-name: ${spring.application.name}
```