package com.baidu.idl.face.main.api;

import com.baidu.idl.face.main.model.AccessToken;
import com.baidu.idl.face.main.utils.HttpUtil;
import com.baidu.idl.face.main.utils.OnResultListener;

public class ApiService {

    private static final String BASE_URL = "https://aip.baidubce.com";

    private static final String ACCESS_TOKEN_URL = BASE_URL + "/oauth/2.0/token?";

    private String accessToken;

    private String groupId;
    private static volatile ApiService instance;


    private ApiService() {

    }

    public static ApiService getInstance() {
        if (instance == null) {
            synchronized (ApiService.class) {
                if (instance == null) {
                    instance = new ApiService();


                }
            }
        }
        return instance;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }



    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, String ak,
                                        String sk) {
        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOKEN_URL, sb.toString());
    }
}
