package cn.tongdun.yuntu.nebulausage.common;


import com.vesoft.nebula.meta.ErrorCode;

/**
 * @author liuyou
 * @date 2021/4/1
 */
public class GraphDatabaseException extends Exception {

    int code = ErrorCode.E_UNKNOWN;

    public GraphDatabaseException(String s) {
        super(s);
    }

    public GraphDatabaseException(Throwable t) {
        super(t);
    }

    public GraphDatabaseException(int code, String msg) {
        super("code:" + code + ",msg:" + msg);
        code = code;
    }

    public int getCode() {
        return code;
    }

}

