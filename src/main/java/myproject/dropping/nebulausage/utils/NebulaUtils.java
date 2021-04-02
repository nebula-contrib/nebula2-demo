package myproject.dropping.nebulausage.utils;

import myproject.dropping.nebulausage.entity.EdgeDO;
import myproject.dropping.nebulausage.entity.PathDO;
import myproject.dropping.nebulausage.entity.TagDO;
import myproject.dropping.nebulausage.entity.VertexDO;
import com.vesoft.nebula.Edge;
import com.vesoft.nebula.NList;
import com.vesoft.nebula.NMap;
import com.vesoft.nebula.Path;
import com.vesoft.nebula.Step;
import com.vesoft.nebula.Tag;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Nebula结果集到DO对象的转换工具类
 *
 * @author dropping
 * @date 2021/4/1
 */
public class NebulaUtils {

    /**
     * 将Nebula Client的 vertex 对象转换为 VertexDO
     *
     * @param vertex 原对象
     * @return VertexDO
     */
    public static VertexDO fromVertex(Vertex vertex) {
        VertexDO result = new VertexDO();
        Object vertexId = vertex.vid.getFieldValue();
        ArrayList tags = new ArrayList<>();
        for (Tag tag : vertex.tags) {
            tags.add(fromTag(vertexId, tag));
        }
        result.setVertexId(vertexId);
        result.setTags(tags);
        return result;
    }

    /**
     * 将Nebula Client的 tag 对象转换为 TagDO
     *
     * @param vertexId 点ID
     * @param tag      原对象
     * @return TagDO
     */
    public static TagDO fromTag(Object vertexId, Tag tag) {
        TagDO tagDO = new TagDO();
        String tagName = new String(tag.name);
        tagDO.setTagName(tagName);
        HashMap props = new HashMap<>();
        if (tag.props != null) {
            for (byte[] key : tag.props.keySet()) {
                props.put(new String(key), fromValue(tag.props.get(key)));
            }
        }
        tagDO.setVertexId(vertexId);
        tagDO.setProps(props);
        return tagDO;
    }

    /**
     * 将Nebula Client的 edge 对象转换为 EdgeDO
     *
     * @param edge 原对象
     * @return EdgeDO
     */
    public static EdgeDO fromEdge(Edge edge) {
        EdgeDO edgeDO = new EdgeDO();
        Object src = fromValue(edge.src);
        if (src instanceof Vertex) {
            edgeDO.setSrcId(((VertexDO) src).getVertexId());
        } else {
            edgeDO.setSrcId(src);
        }
        Object dst = fromValue(edge.dst);
        if (dst instanceof Vertex) {
            edgeDO.setDstId(((VertexDO) dst).getVertexId());
        } else {
            edgeDO.setDstId(dst);
        }
        HashMap props = new HashMap<>();
        if (edge.props != null) {
            for (byte[] key : edge.props.keySet()) {
                props.put(new String(key), fromValue(edge.props.get(key)));
            }
        }
        edgeDO.setRank(edge.ranking);
        edgeDO.setEdgeType(new String(edge.name));
        edgeDO.setProps(props);
        return edgeDO;
    }

    /**
     * 将Nebula Client的 path 对象转换为 PathDO
     *
     * @param path 原对象
     * @return PathDO
     */
    public static PathDO fromPath(Path path) {
        PathDO pathDO = new PathDO();
        ArrayList vertexDTOs = new ArrayList();
        ArrayList edgeDOs = new ArrayList();
        List<Step> steps = path.getSteps();
        VertexDO srcVertexDO = fromVertex(path.src);
        vertexDTOs.add(srcVertexDO);
        Object lastSrcVertexId = srcVertexDO.getVertexId();
        for (Step step : steps) {
            VertexDO newVertexDO = fromVertex(step.dst);
            vertexDTOs.add(newVertexDO);
            EdgeDO edgeDO = fromStep(step);
            edgeDO.setSrcId(lastSrcVertexId);
            edgeDO.setDstId(newVertexDO.getVertexId());
            lastSrcVertexId = newVertexDO.getVertexId();
            edgeDOs.add(edgeDO);
        }
        pathDO.setVertexDOS(vertexDTOs);
        pathDO.setEdgeDOS(edgeDOs);
        return pathDO;
    }

    /**
     * 将Nebula Client的 step 对象转换为 EdgeDO
     * 此为私有方法，因为转换后的对象并没有设置起点和终点点ID
     *
     * @param step 原对象
     * @return EdgeDO
     */
    private static EdgeDO fromStep(Step step) {
        EdgeDO edgeDO = new EdgeDO();
        HashMap props = new HashMap<>();
        if (step.props != null) {
            for (byte[] key : step.props.keySet()) {
                props.put(new String(key), fromValue(step.props.get(key)));
            }
        }
        edgeDO.setEdgeType(new String(step.name));
        edgeDO.setRank(step.ranking);
        edgeDO.setProps(props);
        return edgeDO;
    }

    /**
     * 将Nebula Client的 value 对象根据实际类型转换为相应的对象
     * 包括字符串、点、边、路径、列表、Map等
     *
     * @param value 原对象
     * @return 实际对象类型
     */
    public static Object fromValue(Value value) {
        Object fieldValue = value.getFieldValue();
        int setField = value.getSetField();
        switch (setField) {
            case Value.SVAL:
                fieldValue = new String((byte[]) fieldValue);
                break;
            case Value.VVAL:
                fieldValue = fromVertex(value.getVVal());
                break;
            case Value.EVAL:
                fieldValue = fromEdge(value.getEVal());
                break;
            case Value.PVAL:
                fieldValue = fromPath(value.getPVal());
                break;
            case Value.LVAL:
            case Value.UVAL:
                NList list = value.getLVal();
                ArrayList childList = new ArrayList();
                for (Value childValue : list.values) {
                    childList.add(fromValue(childValue));
                }
                fieldValue = childList;
                break;
            case Value.MVAL:
                NMap mVal = value.getMVal();
                HashMap map = new HashMap();
                for (byte[] skey : mVal.kvs.keySet()) {
                    map.put(new String(skey), fromValue(mVal.kvs.get(skey)));
                }
                fieldValue = map;
                break;
        }
        return fieldValue;
    }

}