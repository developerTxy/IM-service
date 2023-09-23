package com.txy.im.service.user.dao;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
* 
* @TableName im_user_data
*/
@TableName("im_user_data")
public class  ImUserDataEntity {

    /**
    * 
    */

    private Integer appId;
    /**
    * 用户id
    */

    private String userId;
    /**
    * 昵称
    */

    private String nickName;
    /**
    * 性别  1 男  2 女 0 未设置
    */

    private Integer userSex;
    /**
    * 生日
    */

    private String birthDay;
    /**
    * 所在地
    */

    private String location;
    /**
    * 个性签名
    */

    private String selfSignature;
    /**
    * 朋友允许类型  1：无需验证 2：需要验证
    */

    private Integer firendAllowType;
    /**
    * 头像地址
    */

    private String photo;
    /**
    * 密码
    */

    private String password;
    /**
    * 管理员静止用户添加好友 ： 0未经用 1 已禁用

    private Integer disableAddFirend;
    /**
    * 禁言标识 1 禁言
    */

    private Integer silentFlag;
    /**
    * 禁用标识 1 禁用
    */

    private Integer forbiddenFlag;

    private Integer disableAddFirend;
    /**
    * 用户类型 1 im用户
    */

    private Integer userType;
    /**
    * 删除标识 1 删除
    */

    private Integer delFlag;
    /**
    * 扩展
    */

    private String extra;

    /**
    * 
    */
    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getUserSex() {
        return userSex;
    }

    public void setUserSex(Integer userSex) {
        this.userSex = userSex;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSelfSignature() {
        return selfSignature;
    }

    public void setSelfSignature(String selfSignature) {
        this.selfSignature = selfSignature;
    }

    public Integer getFirendAllowType() {
        return firendAllowType;
    }

    public void setFirendAllowType(Integer firendAllowType) {
        this.firendAllowType = firendAllowType;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getSilentFlag() {
        return silentFlag;
    }

    public void setSilentFlag(Integer silentFlag) {
        this.silentFlag = silentFlag;
    }

    public Integer getForbiddenFlag() {
        return forbiddenFlag;
    }

    public void setForbiddenFlag(Integer forbiddenFlag) {
        this.forbiddenFlag = forbiddenFlag;
    }

    public Integer getDisableAddFirend() {
        return disableAddFirend;
    }

    public void setDisableAddFirend(Integer disableAddFirend) {
        this.disableAddFirend = disableAddFirend;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
