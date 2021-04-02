package myproject.dropping.nebulausage.entity;

import lombok.Data;

import java.util.List;

/**
 * 点数据结构
 *
 * @author dropping
 * @date 2021/4/1
 */
@Data
public class VertexDO {
    /**
     * 点ID，类型可以为Long型或者String类型
     */
    private Object vertexId;
    /**
     * 点类型（标签）集合
     */
    private List<TagDO> tags;
}
