package com.ndkey.demo.overview.pojo;


/**
 * @author tangbu
 */
public class RpcResponse  {

    private String requestId;
    private boolean success;
    private String message;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
