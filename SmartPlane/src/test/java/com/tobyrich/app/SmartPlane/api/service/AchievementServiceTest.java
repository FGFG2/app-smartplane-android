package com.tobyrich.app.SmartPlane.api.service;

import android.util.Log;

import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.model.Achievement;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AchievementServiceTest extends TestCase {

    public static final String URL_ALL_ACHIEVEMENTS = "http://chaos-krauts.de/Achievment/";
    private Retrofit retrofit;

    @Before
    public void setUp() throws Exception {
        retrofit = new Retrofit.Builder()
                .baseUrl(URL_ALL_ACHIEVEMENTS)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    @Test
    public void testSendMotorDataInteger() throws Exception {
        // Given
        Map<Long, Short> map = new LinkedHashMap<Long, Short>();
        map.put(5L, (short) 5);

        // When
        AchievementService service = retrofit.create(AchievementService.class);
        Call<?> call = service.setMotor(map);
        Response<?> response = call.execute();

        // Then
        assertTrue(response.isSuccess());
        Log.wtf(this.getClass().getSimpleName(), "Got response with code: " + response.code());
    }

    @Test
    public void testGetAllAchievements() throws Exception {
        // When
        AchievementService service = retrofit.create(AchievementService.class);
        Call<List<Achievement>> call = service.getAllAchievements();
        List<Achievement> achievementList = call.execute().body();

        // Then
        assertFalse(achievementList.isEmpty());
        Log.wtf(this.getClass().getSimpleName(), achievementList.toString());
    }
}