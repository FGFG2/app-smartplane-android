package com.tobyrich.app.SmartPlane.api.service;

import com.tobyrich.app.SmartPlane.api.model.Token;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface UserService {
    @FormUrlEncoded
    @POST("token")
    Call<Token> login(@Field("grant_type") String grantType, @Field("username") String email, @Field("password") String pw);
}
