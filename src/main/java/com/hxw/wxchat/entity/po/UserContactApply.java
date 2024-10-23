package com.hxw.wxchat.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hxw.wxchat.entity.enums.UserContactApplyStatusEnum;
import lombok.Setter;

import java.io.Serializable;


/**
 * 联系人申请
 * @author qw1500292505
 */
@Setter
public class UserContactApply implements Serializable {


	/**
	 * 自增 ID
	 */
	private Integer applyId;

	/**
	 * 申请人ID
	 */
	private String applyUserId;

	/**
	 * 接收人ID
	 */
	private String receiveUserId;

	/**
	 * 联系人群组ID
	 */
	private Integer contactType;

	/**
	 * 联系人群组ID
	 */
	private String contactId;

	/**
	 * 最后申请时间
	 */
	private Long lastApplyTime;

	/**
	 * 状态0:待处理 1:已同意 2:已拒绝 3: 已拉黑
	 */
	private Integer status;

	/**
	 * 申请信息
	 */
	private String applyInfo;
	private String contactName;

	private String statusName;

	public String getStatusName() {
		UserContactApplyStatusEnum statusEnum=UserContactApplyStatusEnum.getByStatus(status);
		return statusName==null?null:statusEnum.getDesc();
	}

	public String getContactName() {
		return contactName;
	}

	public Integer getApplyId(){
		return this.applyId;
	}

	public String getApplyUserId(){
		return this.applyUserId;
	}

	public String getReceiveUserId(){
		return this.receiveUserId;
	}

	public Integer getContactType(){
		return this.contactType;
	}

	public String getContactId(){
		return this.contactId;
	}

	public Long getLastApplyTime(){
		return this.lastApplyTime;
	}

	public Integer getStatus(){
		return this.status;
	}

	public String getApplyInfo(){
		return this.applyInfo;
	}

	@Override
	public String toString (){
		return "自增 ID:"+(applyId == null ? "空" : applyId)+"，申请人ID:"+(applyUserId == null ? "空" : applyUserId)+"，接收人ID:"+(receiveUserId == null ? "空" : receiveUserId)+"，联系人群组ID:"+(contactType == null ? "空" : contactType)+"，联系人群组ID:"+(contactId == null ? "空" : contactId)+"，最后申请时间:"+(lastApplyTime == null ? "空" : lastApplyTime)+"，状态0:待处理 1:已同意 2:已拒绝 3: 已拉黑:"+(status == null ? "空" : status)+"，申请信息:"+(applyInfo == null ? "空" : applyInfo);
	}
}
