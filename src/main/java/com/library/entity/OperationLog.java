package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "operation_log", indexes = {
        @Index(name = "idx_log_user", columnList = "userId"),
        @Index(name = "idx_log_time", columnList = "createTime")
})
public class OperationLog extends BaseEntity {

    @Column(name = "userId", length = 36)
    private String userId;

    @Column(name = "userName", length = 60)
    private String userName;

    @Column(name = "module", length = 40)
    private String module;

    @Column(name = "action", length = 60)
    private String action;

    @Column(name = "requestUri", length = 200)
    private String requestUri;

    @Column(name = "httpMethod", length = 10)
    private String httpMethod;

    @Column(name = "params", length = 2000)
    private String params;

    @Column(name = "ip", length = 60)
    private String ip;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "errorMessage", length = 500)
    private String errorMessage;

    @Column(name = "durationMs")
    private Long durationMs;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}