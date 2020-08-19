/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.model;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Objects;

/**
 * 用户实体类
 */
public class User {
    @Expose(serialize = false, deserialize = false)
    private int id;
    @SerializedName("user_id")
    private String userId = "";
    @SerializedName("user_name")
    private String userName = "";
    @SerializedName("user_info")
//    @Expose(serialize = false, deserialize = false)
    private String userInfo = "";
    @SerializedName("group_id")
    private String groupId = "";
    @SerializedName("item_eid")
    private String itemEId = "";

    @SerializedName("audience_code")
    private String audienceCode;

    @SerializedName("idCardNo")
    private String idCardNo;

    @SerializedName("faceToken")
    private String faceToken = "";
    @SerializedName("regdate")
    private String createTime;
    //    @SerializedName("regdate")
    @Expose(serialize = false, deserialize = false)
    private String updateTime;


    private String image;
    @Expose(serialize = false, deserialize = false)
    private String imageName = "";
    @Expose(serialize = false, deserialize = false)
    private byte[] feature;
    @Expose(serialize = false, deserialize = false)
    private boolean isChecked;

    public User() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getFaceToken() {
        if (feature != null) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getItemEId() {
        return itemEId;
    }

    public void setItemEId(String itemEId) {
        this.itemEId = itemEId;
    }

    public String getAudienceCode() {
        return audienceCode;
    }

    public void setAudienceCode(String audienceCode) {
        this.audienceCode = audienceCode;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return userId.equals(user.getUserId()) && faceToken.equals(user.getFaceToken());

//        return id == user.id &&
//                ctime == user.ctime &&
//                updateTime == user.updateTime &&
//                isChecked == user.isChecked &&
//                Objects.equals(userId, user.userId) &&
//                Objects.equals(userName, user.userName) &&
//                Objects.equals(groupId, user.groupId) &&
//                Objects.equals(userInfo, user.userInfo) &&
//                Objects.equals(faceToken, user.faceToken) &&
//                Objects.equals(imageName, user.imageName) &&
//                Arrays.equals(feature, user.feature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, userId, userName, groupId, createTime, updateTime, userInfo, faceToken, imageName, isChecked);
        result = 31 * result + Arrays.hashCode(feature);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", groupId='" + groupId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", userInfo='" + userInfo + '\'' +
                ", faceToken='" + faceToken + '\'' +
                ", image='" + image + '\'' +
                ", imageName='" + imageName + '\'' +
                ", feature=" + Arrays.toString(feature) +
                ", isChecked=" + isChecked +
                '}';
    }
}
