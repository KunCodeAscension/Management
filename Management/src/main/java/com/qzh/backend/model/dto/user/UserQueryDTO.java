package com.qzh.backend.model.dto.user;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户列表查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryDTO extends PageRequest implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名模糊查询
     */
    private String userName;

    /**
     * 用户账号模糊查询
     */
    private String userAccount;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    private Date startTime;

    private Date endTime;

    public static QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(userQueryDTO == null){
            return queryWrapper;
        }
        String sortField = userQueryDTO.getSortField();
        String sortOrder = userQueryDTO.getSortOrder();
        String userName = userQueryDTO.getUserName();
        String userAccount = userQueryDTO.getUserAccount();
        Date startTime = userQueryDTO.getStartTime();
        Date endTime = userQueryDTO.getEndTime();
        Integer status = userQueryDTO.getStatus();
        queryWrapper.like(ObjUtil.isNotNull(userName),"UserName",userName);
        queryWrapper.like(ObjUtil.isNotNull(userAccount),"UserAccount",userAccount);
        queryWrapper.eq(ObjUtil.isNotNull(status),"status",status);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        return queryWrapper;
    }
}