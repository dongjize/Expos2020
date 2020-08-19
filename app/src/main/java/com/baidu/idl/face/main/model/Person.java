package com.baidu.idl.face.main.model;

import com.google.gson.annotations.SerializedName;

public class Person {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_info")
    private String username;

    @SerializedName("group_id")
    private String groupId;

    private String image;

    @SerializedName("item_eid")
    private int itemEid;

    @SerializedName("audience_code")
    private String audienceCode;

    @SerializedName("idCardNo")
    private String idCardNo;

    @SerializedName("faceToken")
    private String faceToken;

    @SerializedName("regdate")
    private String registerDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getItemEid() {
        return itemEid;
    }

    public void setItemEid(int itemEid) {
        this.itemEid = itemEid;
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

    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }
}
