package cn.tongdun.yuntu.nebulausage.service.impl;

import cn.tongdun.yuntu.nebulausage.common.GraphDatabaseException;
import cn.tongdun.yuntu.nebulausage.config.NebulaConfig;
import cn.tongdun.yuntu.nebulausage.dto.NebulaResultSet;
import cn.tongdun.yuntu.nebulausage.service.NebulaService;
import cn.tongdun.yuntu.nebulausage.utils.NebulaUtils;
import com.vesoft.nebula.Row;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import com.vesoft.nebula.meta.ErrorCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuyou
 * @date 2021/3/26
 */
@Service
@Slf4j
public class NebulaServiceImpl implements NebulaService, DisposableBean {
    @Autowired
    @Setter
    NebulaConfig nebulaConfig;

    @Override
    public NebulaResultSet executeQuery(String spaceName, String statement) throws GraphDatabaseException {
        log.info("查询子句: 图谱:{}, 子句:{}", spaceName, statement);
        NebulaResultSet nebulaResultSet = new NebulaResultSet();
        Session session = null;
        try {
            session = getOrCreateSession(spaceName);
            ResultSet resultSet = session.execute(statement);
            if (!resultSet.isSucceeded()) {
                log.error("查询出错: {}, 错误原因: {}",
                        statement, resultSet.getErrorMessage());
                return nebulaResultSet;
            }
            if (resultSet.isEmpty()) {
                return nebulaResultSet;
            }

            nebulaResultSet.setColumns(resultSet.keys());
            List<String> cols = resultSet.keys();
            List<Row> rows = resultSet.getRows();
            if (!rows.isEmpty()) {
                for (Row row : rows) {
                    Map<String, Object> props = new HashMap<>();
                    List<com.vesoft.nebula.Value> rowValues = row.getValues();
                    for (int i = 0; i < cols.size(); i++) {
                        String key = cols.get(i);
                        props.put(key, NebulaUtils.fromValue(rowValues.get(i)));
                    }
                    nebulaResultSet.getRows().add(props);
                }
            }

        } catch (Throwable e) {
            log.error("执行异常!", e);
            throw new GraphDatabaseException(e);
        } finally {
            if (session != null) {
                session.release();
            }
        }

        return nebulaResultSet;
    }

    @Override
    public int execute(String spaceName, String statement) throws GraphDatabaseException {
        log.info("执行子句: 图谱:{}, 子句:{}", spaceName, statement);
        Session session = null;
        try {
            session = getOrCreateSession(spaceName);
            ResultSet resultSet = session.execute(statement);
            if (resultSet.isSucceeded()) {
                return ErrorCode.SUCCEEDED;
            }
            log.warn("执行出错:{}, {}", resultSet.getErrorCode(), resultSet.getErrorMessage());
            return resultSet.getErrorCode();
        } catch (Throwable e) {
            log.error("执行异常!", e);
            throw new GraphDatabaseException(e);
        } finally {
            if (session != null) {
                session.release();
            }
        }
    }

    private Session getOrCreateSession(String spaceName) throws UnknownHostException, NotValidConnectionException, IOErrorException, AuthFailedException, UnsupportedEncodingException, GraphDatabaseException {
        Session session = getOrCreateSession();
        if (spaceName != null) {
            ResultSet resultSet = session.execute("use " + spaceName + ";");
            if (!resultSet.isSucceeded()) {
                throw new GraphDatabaseException(resultSet.getErrorCode(), resultSet.getErrorMessage());
            }
        }
        return session;
    }

    private Session getOrCreateSession() throws UnknownHostException, NotValidConnectionException, IOErrorException, AuthFailedException, GraphDatabaseException {
        Session session = getNebulaPool().getSession(nebulaConfig.getUser(), nebulaConfig.getPassword(), true);
        return session;
    }

    public void closeNebulaPool() {
        if (NEBULA_PULL != null) {
            log.info("关闭连接池");
            NEBULA_PULL.close();
        }
    }

    private NebulaPool getNebulaPool() throws UnknownHostException, GraphDatabaseException {
        if (NEBULA_PULL == null) {
            synchronized (this) {
                if (NEBULA_PULL == null) {
                    NEBULA_PULL = new NebulaPool();
                    NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
                    nebulaPoolConfig.setMinConnSize(nebulaConfig.getPoolInitNum());
                    nebulaPoolConfig.setIdleTime(nebulaConfig.getExecuteTimeout() * 10);
                    nebulaPoolConfig.setTimeout(nebulaConfig.getExecuteTimeout());
                    nebulaPoolConfig.setMaxConnSize(nebulaConfig.getPoolMaxNum());
                    NEBULA_PULL.init(nebulaConfig.getHostList(), nebulaPoolConfig);
                    log.info("初始化连接池：{}", nebulaConfig.getHosts());
                }
            }
        }
        return NEBULA_PULL;
    }

    private static NebulaPool NEBULA_PULL = null;

    @Override
    public void destroy() throws Exception {
        closeNebulaPool();
    }
}
