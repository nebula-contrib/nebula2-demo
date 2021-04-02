package cn.tongdun.yuntu.nebulausage.entity;

import lombok.Data;

import java.util.List;

/**
 * 单条路径结构
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Data
public class PathDO {
    /**
     * 按从起点到终点顺序存储的点的数据
     */
    private List<VertexDO> vertexDOS;
    /**
     * 按第一条边到最后一条边存储的边的数据
     */
    private List<EdgeDO> edgeDOS;
}
