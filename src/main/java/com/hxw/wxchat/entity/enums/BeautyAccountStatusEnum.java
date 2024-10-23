package com.hxw.wxchat.entity.enums;


import javax.persistence.criteria.CriteriaBuilder;

public enum BeautyAccountStatusEnum {
    NO_USE(0,"未使用"),
        USEED(1,"已使用");
    private Integer status;
    private String desc;

     BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
    public static BeautyAccountStatusEnum getByStatus(Integer status){
         for (BeautyAccountStatusEnum item:BeautyAccountStatusEnum.values()){
             if (item.getStatus().equals(status)){
                 return item;
             }
         }
         return null;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
