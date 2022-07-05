package com.guo.gulimall.auth.vo;

import lombok.Data;

@Data
public class OAuthGitHubResponse {

    private String access_token;
    private String scope;
    private String token_type;
}
