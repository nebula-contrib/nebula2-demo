package myproject.dropping.nebulausage.service;

import myproject.dropping.nebulausage.common.GraphDatabaseException;
import myproject.dropping.nebulausage.dto.NebulaResultSet;

/**
 * Nebula服务层接口
 *
 * @author dropping
 * @date 2021/4/1
 */
public interface NebulaService {
    /**
     * 查询语句的执行，将返回数据
     *
     * @param spaceName 图谱名称
     * @param statement 查询语句
     * @return 图数据的封装结果
     * @throws GraphDatabaseException 异常信息
     */
    NebulaResultSet executeQuery(String spaceName, String statement) throws GraphDatabaseException;

    /**
     * DDL和DML语句的执行，将返回状态值
     *
     * @param spaceName 图谱名称
     * @param statement DDL或DML
     * @return 状态值
     * @throws GraphDatabaseException 异常信息
     */
    int execute(String spaceName, String statement) throws GraphDatabaseException;

}
