package com.hxw.wxchat.entity.vo;

import lombok.Data;

@Data
public class ResultResponse<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    public final static String SUCCESS="success";
    public final static int SUCCESS_CODE=200;
    public final static String FILE="file";
    public final static int FILE_CODE=-1;


    public ResultResponse(String status, Integer code, String info, T data) {
        this.status = status;
        this.code = code;
        this.info = info;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status='" + status + '\'' +
                ", code=" + code +
                ", info='" + info + '\'' +
                ", data=" + data +
                '}';
    }
}
