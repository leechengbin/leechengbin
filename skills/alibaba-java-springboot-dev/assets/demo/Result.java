package com.company.project.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 * <p>所有 Controller 接口的返回值都应使用此类进行封装</p>
 *
 * @param <T> 数据类型
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 序列化版本 ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     * <ul>
     *     <li>200 - 成功</li>
     *     <li>400 - 请求参数错误</li>
     *     <li>401 - 未授权</li>
     *     <li>403 - 禁止访问</li>
     *     <li>404 - 资源不存在</li>
     *     <li>500 - 系统内部错误</li>
     * </ul>
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 数据
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     *
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     *
     * @param message 错误消息
     * @return 响应结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（带错误码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
