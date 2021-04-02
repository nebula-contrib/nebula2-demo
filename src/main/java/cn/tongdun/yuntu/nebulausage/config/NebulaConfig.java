package cn.tongdun.yuntu.nebulausage.config;

import cn.tongdun.yuntu.nebulausage.common.GraphDatabaseException;
import com.vesoft.nebula.client.graph.data.HostAddress;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Nebula配置对象
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Data
@Component
public class NebulaConfig {
    /**
     * Nebula graphd集群地址，以逗号或者分号分隔，如10.58.14.34:3699;10.58.14.35:3699
     */
    @Value("${nebula.hosts}")
    private String hosts;
    /**
     * 图空间的名称在集群中标明了一个唯一的空间
     */
    @Value("${nebula.spaceName}")
    private String spaceName;
    /**
     * 连接图库的用户名
     */
    @Value("${nebula.user:root}")
    private String user;
    /**
     * 连接图库的密码
     */
    @Value("${nebula.password:nebula}")
    private String password;
    /**
     * 执行超时时间，单位为ms
     */
    @Value("${nebula.executeTimeout:60000}")
    private int executeTimeout;
    /**
     * 连接池初始值
     */
    @Value("${nebula.poolInitNum:5}")
    private int poolInitNum;
    /**
     * 连接池最大值
     */
    @Value("${nebula.poolMaxNum:20}")
    private int poolMaxNum;

    /**
     * 通过将字符串服务器列表转换为HostAddress对象
     *
     * @return 服务器列表
     * @throws GraphDatabaseException 参数异常
     */
    public List<HostAddress> getHostList() throws GraphDatabaseException {
        try {
            List<HostAddress> list = new ArrayList<>();
            String[] arr = hosts.split(";|,");
            for (String s : arr) {
                String[] pair = s.split(":");
                String host = pair[0];
                Integer port = 3699;
                if (pair.length == 2) {
                    port = Integer.parseInt(pair[1]);
                }
                list.add(new HostAddress(host, port));
            }
            return list;
        } catch (Throwable t) {
            throw new GraphDatabaseException("nebula.hosts参数配置不正确，请检查。");
        }
    }
}