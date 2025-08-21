## 魔改模块

spring-ai-spring-boot-starters/spring-ai-starter-mcp-server-webmvc
mcp/common
auto-configurations/mcp/spring-ai-autoconfigure-mcp-server

## 安装步骤

为了避免 Maven Reactor 机制导致的兄弟模块版本号设定失效，故采用单独打包发布方式，步骤如下：

```shell
./mvnw -pl :spring-ai-mcp -DskipTests clean install

./mvnw -pl :spring-ai-autoconfigure-mcp-server -DskipTests clean install

./mvnw -pl :spring-ai-starter-mcp-server-webmvc -DskipTests clean install
```

## 依赖处理

由于该魔改版本需要依赖 io.modelcontextprotocol.sdk 库对应的魔改版本，故应该先在本地 Maven 仓库中安装对应依赖包：

mcp-spring-webmvc:0.12.0-SNAPSHOT-IWC
mcp:0.12.0-SNAPSHOT-IWC