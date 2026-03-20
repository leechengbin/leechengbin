package com.satcom.platform.exception;

/**
 * 业务异常类
 * <p>用于抛出业务相关的异常信息</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
