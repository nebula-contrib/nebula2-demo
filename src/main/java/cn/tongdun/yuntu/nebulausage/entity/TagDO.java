package cn.tongdun.yuntu.nebulausage.entity;

import lombok.Data;

import java.util.Map;

/**
 * 标签数据结构
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Data
public class TagDO {
    /**
     * 点ID
     */
    private Object vertexId;
    /**
     * 标签名
     */
    private String tagName;
    /**
     * 属性映射
     */
    private Map<String, Object> props;
}
