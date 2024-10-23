package com.hxw.wxchat.entity.enums;

import com.hxw.wxchat.utils.StringTools;

public enum JoinTypeEnum {
    JOIN(0,"直接加入"),
    APPLY(1,"需要审核");

    JoinTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    private Integer type;
    private String desc;
    public static JoinTypeEnum getByName(String name){
       try {
           if (StringTools.isEmpty(name)){
               return null;
           }
           return JoinTypeEnum.valueOf(name.toUpperCase());
       }catch (IllegalArgumentException e){
           return null;
       }

    }
    public static JoinTypeEnum getByType(Integer joinType){
        for (JoinTypeEnum joinTypeEnum:JoinTypeEnum.values()){
            if (joinTypeEnum.getType().equals(joinType)){
                return joinTypeEnum;
            }
        }
        return null;
    }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
