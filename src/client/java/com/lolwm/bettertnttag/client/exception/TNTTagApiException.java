package com.lolwm.bettertnttag.client.exception;

public class TNTTagApiException extends Exception {
    private final int statusCode;
    private final String errorMessage;
    private final Integer errorCode;

    public TNTTagApiException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.errorCode = null;
    }

    public TNTTagApiException(int statusCode, String errorMessage, Integer errorCode) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "TNTTagApiException{" +
                "statusCode=" + statusCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }
}
