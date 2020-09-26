package com.example.mediodownload.model;

import java.io.Serializable;

public class RespModel implements Serializable {

    private String code = "00";
    private String msg = "SUCCESS";
    private String url = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "RespModel{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
