package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user_role")
public class UserRelatedRole implements Serializable {

    private Long userId;

    private Long roleId;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

}