package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.query.GroupInfoQuery;
import com.hxw.wxchat.entity.query.UserContactQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 群组 数据库操作接口
 */
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

	/**
	 * 根据GroupId更新
	 */
	@Update("UPDATE group_info SET group_name = #{bean.groupName}, group_owner_id = #{bean.groupOwnerId}, create_time = #{bean.createTime}, group_notice = #{bean.groupNotice}, join_type = #{bean.joinType}, status = #{bean.status} WHERE group_id = #{groupId}")
	 Integer updateByGroupId(@Param("bean") GroupInfo groupInfo,@Param("groupId") String groupId);


	/**
	 * 根据GroupId删除
	 */
	@Delete("DELETE FROM group_info WHERE group_id = #{groupId}")
	 Integer deleteByGroupId(@Param("groupId") String groupId);


	/**
	 * 根据GroupId获取对象
	 */
	@Select("SELECT * FROM group_info WHERE group_id = #{groupId}")
	GroupInfo selectByGroupId(@Param("groupId") String groupId);

	List<GroupInfo> selectList(@Param("query") GroupInfoQuery query);


}
