package com.github.catvod.bean.ali;

import android.text.TextUtils;

import com.github.catvod.utils.Prefers;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("default_drive_id")
    private String driveId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;

    public static User objectFrom(String str) {
        User item = new Gson().fromJson(str, User.class);
        return item == null ? new User() : item;
    }

    public String getDriveId() {
        return TextUtils.isEmpty(driveId) ? "" : driveId;
    }

    public String getUserId() {
        return TextUtils.isEmpty(userId) ? "" : userId;
    }

    public String getTokenType() {
        return TextUtils.isEmpty(tokenType) ? "" : tokenType;
    }

    public String getAccessToken() {
        return TextUtils.isEmpty(accessToken) ? "" : accessToken;
    }

    public String getRefreshToken() {
        return TextUtils.isEmpty(refreshToken) ? "" : refreshToken;
    }

    public String getAuthorization() {
        return getTokenType() + " " + getAccessToken();
    }

    public User clean() {
        this.refreshToken = "";
        this.accessToken = "";
        return this;
    }

    public User save() {
        Prefers.put("aliyundrive_user", new Gson().toJson(this));
        return this;
    }
}
