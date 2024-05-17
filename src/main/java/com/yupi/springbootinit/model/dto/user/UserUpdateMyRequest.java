package com.yupi.springbootinit.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author 加棉
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户原密码
     */
    private String userPassword;

    /**
     * 修改密码时,第一次输入的密码
     */
    private String newPassword;

    /**
     * 修改密码时,第二次输入的密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 1L;
}
