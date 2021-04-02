package cn.tongdun.yuntu.nebulausage.entity;

import lombok.Data;

import java.util.Map;

/**
 * 边数据结构
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Data
public class EdgeDO {
    /**
     * 起点的点ID
     */
    private Object srcId;
    /**
     * 终点的点ID
     */
    private Object dstId;
    /**
     * 边的顺序（同起点、终点和边类型的情况下可能存在多边，用来区分多边，单边一般为0）
     */
    private Long rank;
    /**
     * 边的类型名称
     */
    private String edgeType;
    /**
     * 边的属性映射
     */
    private Map<String, Object> props;
}
