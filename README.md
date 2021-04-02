# Getting Started

Nebula graph 2.0使用用例

# 项目说明
* Springboot应用提供nebula图库的查询API和DDL、DML API
* test包含Nebula v2.0测试用例

## 测试方法

 **运行 cn.tongdun.yuntu.NebulaServiceMainTest**
 
 用例场景包括：
 * 新建/删除图谱
 * 新建点类型、边类型、索引
 * 插入点数据、边数据、多边数据
 * 获取点属性、查询点数据、获取一度拓展（子图）、查找最短路径
 * 修改点、修改边、删除点、删除边
 * 删除点类型索引、删除点类型

 *运行前，先配置getNebulaConfig中的服务器地址*
 <code>
 NebulaConfig nebulaConfig = new NebulaConfig();
 nebulaConfig.setHosts("10.58.14.36:3699");
 </code>

## 应用运行

修改src/main/resources/application.properties中nebula.hosts参数

`nebula.hosts=10.58.14.36:3699`

在根目录运行

`mvn package`

`java -jar target/nebulav2-usage.jar`

## 浏览器中访问
**查询子图**

http://localhost:8011/nebula/query?sql=get%20subgraph%20from%201

**拓展查询**
http://localhost:8011/nebula/extend?vertexId=1&steps=2
