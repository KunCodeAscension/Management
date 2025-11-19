package com.qzh.backend.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_role")
public class Role implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String roleName;


    private String description;


    private Date createTime;


    private Date updateTime;


    private Long createBy;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}