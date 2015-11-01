package com.tobyrich.app.SmartPlane.api.service;

import com.tobyrich.app.SmartPlane.api.model.Achievement;

import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface AchievementService {
    @GET("api/AllAchievements")
    Call<List<Achievement>> getAllAchievements();

    @POST("api/SetMotor")
    Call<String> setMotor(@Body Map<Long, Short> map);

    @POST("api/SetRuder")
    Call<String> setRudder(@Body Map<Long, Short> map);

    @POST("api/SetIsConnected")
    Call<String> setIsConnected(@Body Map<Long, Boolean> map);
}