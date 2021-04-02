package cn.tongdun.yuntu;

import cn.tongdun.yuntu.nebulausage.common.GraphDatabaseException;
import cn.tongdun.yuntu.nebulausage.config.NebulaConfig;
import cn.tongdun.yuntu.nebulausage.dto.NebulaResultSet;
import cn.tongdun.yuntu.nebulausage.entity.PathDO;
import cn.tongdun.yuntu.nebulausage.entity.VertexDO;
import cn.tongdun.yuntu.nebulausage.service.NebulaService;
import cn.tongdun.yuntu.nebulausage.service.impl.NebulaServiceImpl;
import com.alibaba.fastjson.JSON;
import com.vesoft.nebula.meta.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * 此测试用例为Nebula v2.0使用场景用例，供参考
 *
 * <p>
 * 用例场景包括：
 * 新建/删除图谱
 * 新建点类型、边类型、索引
 * 插入点数据、边数据、多边数据
 * 获取点属性、查询点数据、获取一度拓展（子图）、查找最短路径
 * 修改点、修改边、删除点、删除边
 * 删除点类型索引、删除点类型
 * <p>
 * 运行前，先配置getNebulaConfig中的服务器地址
 * NebulaConfig nebulaConfig = new NebulaConfig();
 * nebulaConfig.setHosts("10.58.14.36:3699");
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Slf4j
public class NebulaServiceMainTest {
    public static void main(String[] args) {
        NebulaConfig nebulaConfig = getNebulaConfig();
        NebulaService nebulaService = getNebulaService(nebulaConfig);
        try {
            runCases(nebulaService, nebulaConfig);
        } catch (Throwable t) {
            log.error("出错啦！可能需要调整一下时间间隔！", t);
        } finally {
            //关闭连接池，spring框架应用中会自动关闭，不需要手动关闭
            ((NebulaServiceImpl) nebulaService).closeNebulaPool();
        }
    }

    public static void runCases(NebulaService nebulaService, NebulaConfig nebulaConfig) throws GraphDatabaseException, InterruptedException {
        int code = 0;

        /**
         * 删除图谱空间
         * drop space if exists mock_space
         */
        System.out.println("-------删除图谱空间-----------");
        nebulaService.execute(null, "drop space if exists " + nebulaConfig.getSpaceName());
        assertTrue(code == ErrorCode.SUCCEEDED);

        Thread.sleep(3 * 1000);

        /**
         * 创建新的图谱空间，副本数不能超过当前集群中存储节点的数量，否则会报错-8，此处点类型为整形
         * create space if not exists mock_space(partition_num=10, replica_factor=1, vid_type=Int64)
         */
        System.out.println("-------新建图谱空间-----------");
        code = nebulaService.execute(null, "create space if not exists  " + nebulaConfig.getSpaceName() + "(partition_num=10, replica_factor=1, vid_type=Int64)");

        assertTrue(code == ErrorCode.SUCCEEDED);

        //等待同步图谱
        System.out.println("-------等待同步图谱-----------");
        Thread.sleep(10 * 1000);

        /**
         * 新建点类型，属性支持default默认值，可以不用在插入时指定值，否则每个字段都需要赋值
         * create tag people(f1 int, f2 string, f3 string default '')
         */
        System.out.println("-------新建点类型-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "create tag people(f1 int, f2 string, f3 string default '')");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 新建边类型
         * create edge follow(f1 int, f2 string)
         */
        System.out.println("-------新建边类型-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "create edge follow(f1 int, f2 string)");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 创建点类型的索引，lookup和match查询需要先创建索引
         * create tag index people_index on people()
         */
        System.out.println("-------创建点类型的索引-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "create tag index people_index on people()");
        assertTrue(code == ErrorCode.SUCCEEDED);

        //等待同步结构
        System.out.println("-------等待同步结构-----------");
        Thread.sleep(60 * 1000);

        /**
         * 批量插入多个点
         * insert vertex people(f1, f2) values 1:(1, 'f2-1'), 2:(2, 'f2-2')
         */
        System.out.println("-------批量插入多个点-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "insert vertex people(f1, f2) values 1:(1, 'f2-1'), 2:(2, 'f2-2')");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 插入单边
         * insert edge follow(f1, f2) values 1->2:(1,'f2-1')
         */
        System.out.println("-------插入单边-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "insert edge follow(f1, f2) values 1->2:(1,'f2-1')");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 插入多边（同起点终点中间有多条同类型的边）
         * insert edge follow(f1, f2) values 2->1@0:(2,'f2-1'),2->1@1:(2,'f2-2')
         */
        System.out.println("-------插入多边-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "insert edge follow(f1, f2) values 2->1@0:(2,'f2-1'),2->1@1:(2,'f2-2')");
        assertTrue(code == ErrorCode.SUCCEEDED);

        NebulaResultSet nebulaResultSet;
        /**
         * 根据点ID获取点的属性
         * fetch prop on people 1
         * +----------------------------------------+
         * | vertices_                              |
         * +----------------------------------------+
         * | (1 :people{f1: 1, f2: "f2-1", f3: ""}) |
         * +----------------------------------------+
         */
        System.out.println("-------根据点ID获取点的属性-----------");
        nebulaResultSet = nebulaService.executeQuery(nebulaConfig.getSpaceName(), "fetch prop on people 1");
        assertTrue(!nebulaResultSet.getRows().isEmpty());
        System.out.println("查询结果:----------");
        for (Map<String, Object> row : nebulaResultSet.getRows()) {
            VertexDO vertexDO = (VertexDO) row.get("vertices_");
            System.out.println(JSON.toJSONString(vertexDO));
        }

        /**
         * lookup语法查找点或者边
         * lookup on people
         * +----------+
         * | VertexID |
         * +----------+
         * | 1        |
         * +----------+
         * | 2        |
         * +----------+
         */
        System.out.println("-------lookup语法查找点或者边-----------");
        nebulaResultSet = nebulaService.executeQuery(nebulaConfig.getSpaceName(), "lookup on people");
        assertTrue(!nebulaResultSet.getRows().isEmpty());
        System.out.println("查询结果:----------");
        for (Map<String, Object> row : nebulaResultSet.getRows()) {
            Object vertexID = row.get("VertexID");
            System.out.println(vertexID);
        }

        /**
         * match语法查找点或者边
         * match (a:people) return a
         * +----------------------------------------+
         * | a                                      |
         * +----------------------------------------+
         * | (1 :people{f1: 1, f2: "f2-1", f3: ""}) |
         * +----------------------------------------+
         * | (2 :people{f1: 2, f2: "f2-2", f3: ""}) |
         * +----------------------------------------+
         */
        System.out.println("-------match语法查找点或者边-----------");
        nebulaResultSet = nebulaService.executeQuery(nebulaConfig.getSpaceName(), "match (a:people) return a");
        assertTrue(!nebulaResultSet.getRows().isEmpty());
        System.out.println("查询结果:----------");
        for (Map<String, Object> row : nebulaResultSet.getRows()) {
            VertexDO vertexDO = (VertexDO) row.get("a");
            System.out.println(JSON.toJSONString(vertexDO));
        }

        /**
         * 拓展子图，支持几度拓展，支持过滤边类型和方向
         * get subgraph 1 steps from 1 BOTH follow
         * +------------------------------------------+-----------------+
         * | _vertices                                | _edges          |
         * +------------------------------------------+-----------------+
         * | [(1 :people{f1: 1, f2: "f2-1", f3: ""})] | [[:fo...]]      |
         * +------------------------------------------+-----------------+
         * | [(2 :people{f1: 2, f2: "f2-2", f3: ""})] | []              |
         * +------------------------------------------+-----------------+
         */
        System.out.println("-------拓展子图-----------");
        nebulaResultSet = nebulaService.executeQuery(nebulaConfig.getSpaceName(), "get subgraph 1 steps from 1 BOTH follow");
        assertTrue(!nebulaResultSet.getRows().isEmpty());
        System.out.println("查询结果:----------");
        for (Map<String, Object> row : nebulaResultSet.getRows()) {
            List<VertexDO> vertexDOS = (List<VertexDO>) row.get("_vertices");
            System.out.println(JSON.toJSONString(vertexDOS));
            List<Object> edgeDTOS = (List<Object>) row.get("_edges");
            System.out.println(JSON.toJSONString(edgeDTOS));
        }

        /**
         * 查找最短路径
         * find shortest path from 1 to 2 over *
         * +----------------------------+
         * | path                       |
         * +----------------------------+
         * | <(1)-[:follow@0 {}]->(2)> |
         * +----------------------------+
         */
        System.out.println("-------查找最短路径-----------");
        nebulaResultSet = nebulaService.executeQuery(nebulaConfig.getSpaceName(), "find shortest path from 1 to 2 over *");
        assertTrue(!nebulaResultSet.getRows().isEmpty());
        System.out.println("查询结果:----------");
        for (Map<String, Object> row : nebulaResultSet.getRows()) {
            PathDO pathDTOS = (PathDO) row.get("path");
            System.out.println(JSON.toJSONString(pathDTOS));
        }

        /**
         * 修改点
         * update vertex 1 set people.f2='f2-2-new'
         */
        System.out.println("-------修改点-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "update vertex 1 set people.f2='f2-2-new'");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 修改边
         * update edge 2->1@1 of follow set f2='f2-2-new'
         */
        System.out.println("-------修改边-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "update edge 2->1@1 of follow set f2='f2-2-new'");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 删除边
         * delete edge follow 2->1@1
         */
        System.out.println("-------删除边-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "delete edge follow 2->1@1");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 删除点
         * delete vertex 1
         */
        System.out.println("-------删除点-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "delete vertex 1");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 删除点类型的索引
         * drop tag index people_index
         */
        System.out.println("-------删除点类型的索引-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "drop tag index people_index");
        assertTrue(code == ErrorCode.SUCCEEDED);

        /**
         * 删除点类型，删点类型前需要删掉索引
         * drop tag if exists people
         */
        System.out.println("-------删除点类型-----------");
        code = nebulaService.execute(nebulaConfig.getSpaceName(), "drop tag if exists people");
        assertTrue(code == ErrorCode.SUCCEEDED);

    }

    /**
     * 构造NebulaServiceImpl对象，在项目中会自动加载
     *
     * @param nebulaConfig 配置对象
     * @return nebulaService对象
     */
    private static NebulaService getNebulaService(NebulaConfig nebulaConfig) {
        NebulaService nebulaService = new NebulaServiceImpl();
        ((NebulaServiceImpl) nebulaService).setNebulaConfig(nebulaConfig);
        return nebulaService;
    }

    /**
     * 测试用例需要手动构造参数，在项目中用配置文件加载
     *
     * @return 配置对象
     */
    private static NebulaConfig getNebulaConfig() {
        NebulaConfig nebulaConfig = new NebulaConfig();
        nebulaConfig.setHosts("10.58.14.36:3699");
        nebulaConfig.setSpaceName("mock_space");
        nebulaConfig.setUser("root");
        nebulaConfig.setPassword("nebula");
        nebulaConfig.setExecuteTimeout(3600 * 1000);
        nebulaConfig.setPoolInitNum(5);
        nebulaConfig.setPoolMaxNum(100);
        return nebulaConfig;
    }
}
