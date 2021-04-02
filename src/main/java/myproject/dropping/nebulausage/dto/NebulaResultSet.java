package myproject.dropping.nebulausage.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nebula返回结果集数据结构
 *
 * @author dropping
 * @date 2021/4/1
 */
@Data
public class NebulaResultSet implements Serializable {
    /**
     * 结构信息
     */
    private List<String> columns = new ArrayList<>();
    /**
     * 具体的字段值映射
     */
    private List<Map<String, Object>> rows = new ArrayList<>();
}