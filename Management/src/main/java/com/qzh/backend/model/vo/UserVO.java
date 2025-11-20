package com.qzh.backend.model.vo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.qzh.backend.model.entity.User;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userAccount;

    private String userName;

    private String phone;

    private String email;

    private List<String> roleNames;

    private Date createTime;

    private Date updateTime;

    public static UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    public static List<UserVO> toUserVOList(List<User> users) {
        if (CollectionUtil.isEmpty(users)) {
            return null;
        }
        return BeanUtil.copyToList(users, UserVO.class);
    }
}
