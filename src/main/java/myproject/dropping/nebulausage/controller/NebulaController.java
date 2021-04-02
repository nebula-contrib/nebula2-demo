package myproject.dropping.nebulausage.controller;

import myproject.dropping.nebulausage.config.NebulaConfig;
import myproject.dropping.nebulausage.dto.ApiResult;
import myproject.dropping.nebulausage.dto.NebulaResultSet;
import myproject.dropping.nebulausage.service.NebulaService;
import com.vesoft.nebula.meta.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author dropping
 */
@RequestMapping("/nebula")
@RestController
@Slf4j
public class NebulaController {
    @Autowired
    private NebulaService nebulaService;

    @Autowired
    private NebulaConfig nebulaConfig;

    @RequestMapping("query")
    @ResponseBody
    public ApiResult query(@RequestParam(name = "space", required = false) String spaceName, @RequestParam(name = "sql") String statement) {
        spaceName = getDefaultSpaceName(spaceName);
        log.info("request query {}, {}", spaceName, statement);
        if (statement == null) {
            return ApiResult.failure("statement is null");
        }
        long start = System.currentTimeMillis();
        String sessionKey = Thread.currentThread().getId() + ":" + statement;
        String md5 = DigestUtils.md5DigestAsHex(sessionKey.getBytes());
        log.info(md5 + "->" + statement);
        try {
            NebulaResultSet resultSet = nebulaService.executeQuery(spaceName, statement);
            HashMap resultObj = new HashMap();
            List<Map<String, String>> columns = new ArrayList<>();
            for (String col : resultSet.getColumns()) {
                Map<String, String> tmp = new HashMap<>();
                tmp.put("dataIndex", col);
                tmp.put("title", col);
                columns.add(tmp);
            }
            resultObj.put("columns", columns);
            resultObj.put("dataSource", resultSet.getRows());
            return ApiResult.successWithResult(resultObj);
        } catch (Exception e) {
            log.error("nebula sql execute fail", e);
            return ApiResult.failure("nebula sql execute fail:" + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            log.info(md5 + "->耗时:" + (end - start) + "ms");
        }
    }

    @RequestMapping("extend")
    @ResponseBody
    public ApiResult extend(@RequestParam(name = "space", required = false) String spaceName, @RequestParam(name = "vertexId") String vertexId, @RequestParam(name = "steps") int steps) {
        //TODO 图谱点ID数据类型为整形数时不需要加引号，其他情况需要加引号
//        return query(spaceName, "get subgraph " + steps + " steps from '" + vertexId + "'");
        return query(spaceName, "get subgraph " + steps + " steps from " + vertexId);
    }

    @RequestMapping("execute")
    @ResponseBody
    public ApiResult execute(@RequestParam(name = "space", required = false) String spaceName, @RequestParam(name = "sql") String statement) {
        spaceName = getDefaultSpaceName(spaceName);
        log.info("request execute {}, {}", spaceName, statement);
        if (statement == null) {
            return ApiResult.failure("statement is null");
        }
        long start = System.currentTimeMillis();
        String sessionKey = Thread.currentThread().getId() + ":" + statement;
        String md5 = DigestUtils.md5DigestAsHex(sessionKey.getBytes());
        log.info(md5 + "->" + statement);
        try {
            int code = nebulaService.execute(spaceName, statement);
            if (code == ErrorCode.SUCCEEDED) {
                return ApiResult.successWithResult(code);
            }
            return ApiResult.failure("code is " + code);
        } catch (Exception e) {
            log.error("nebula sql execute fail", e);
            return ApiResult.failure("nebula sql execute fail:" + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            log.info(md5 + "->耗时:" + (end - start) + "ms");
        }
    }

    private String getDefaultSpaceName(@RequestParam(name = "space", required = false) String spaceName) {
        spaceName = StringUtils.defaultString(spaceName, nebulaConfig.getSpaceName());
        return spaceName;
    }

}

