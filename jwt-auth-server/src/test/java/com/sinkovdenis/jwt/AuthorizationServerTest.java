package com.sinkovdenis.jwt;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationServerTest {
    
    @Test
    public void accessTokenObtained_thenSuccess() {
        String accessToken = obtainAccessToken();
        
        assertThat(accessToken).isNotBlank();
    }

    @Test
    public void serviceStartsAndLoadsRealmConfigurations_thenSuccess() {
        final String oidcDiscoveryUrl = "http://localhost:8083/auth/realms/test/.well-known/openid-configuration";

        Response response = RestAssured.given().redirects().follow(false).get(oidcDiscoveryUrl);

        assertThat(HttpStatus.OK.value()).isEqualTo(response.getStatusCode());
        System.out.println(response.asString());
        assertThat(response.jsonPath().getMap("$.")).containsKeys("issuer", "authorization_endpoint", "token_endpoint",
                "userinfo_endpoint");
    }

    private String obtainAccessToken() {
        final String redirectUrl = "http://localhost:8082/jwt-client/login/oauth2/code/custom";
        final String authorizeUrl = "http://localhost:8083/auth/realms/test/protocol/openid-connect/auth?response_type=code&client_id=jwtClient&scope=read&redirect_uri="
               + redirectUrl;
        final String tokenUrl = "http://localhost:8083/auth/realms/test/protocol/openid-connect/token";

        Response response = RestAssured.given().redirects().follow(false).get(authorizeUrl);
        String authSessionId = response.getCookie("AUTH_SESSION_ID");
        String kcPostAuthenticationUrl = response.asString().split("action=\"")[1].split("\"")[0].replace("&amp;", "&");

        // obtain authentication code and state
        response = RestAssured.given().redirects().follow(false).cookie("AUTH_SESSION_ID", authSessionId)
                .formParams("username", "john@test.com", "password", "123", "credentialId", "")
                .post(kcPostAuthenticationUrl);
        assertThat(HttpStatus.FOUND.value()).isEqualTo(response.getStatusCode());

        // extract authorization code
        String location = response.getHeader(HttpHeaders.LOCATION);
        String code = location.split("code=")[1].split("&")[0];

        // get access token
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", "jwtClient");
        params.put("redirect_uri", redirectUrl);
        params.put("client_secret", "jwtClientSecret");
        response = RestAssured.given().formParams(params).post(tokenUrl);
        return response.jsonPath().getString("access_token");

    }
}
