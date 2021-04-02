package cn.tongdun.yuntu.nebulausage.dto;

import lombok.Data;

/**
 * Web层统一封装的API接口返回值数据结构
 *
 * @author liuyou
 * @date 2021/4/1
 */
@Data
public class ApiResult<T> {
    /**
     * 结果对象
     */
    private T result;
    /**
     * 返回代码
     */
    private int code = 200;
    /**
     * 出错原因
     */
    private String reason;

    /**
     * 成功返回结果
     *
     * @param result 正确的结果对象
     * @param <T>    结果类型
     * @return ApiResult
     */
    public static <T> ApiResult<T> successWithResult(T result) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setResult(result);
        return apiResult;
    }

    /**
     * 返回失败信息
     *
     * @param message 失败的原因
     * @param <T>     结果类型
     * @return ApiResult
     */
    public static <T> ApiResult failure(String message) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(500);
        apiResult.setReason(message);
        return apiResult;
    }

}