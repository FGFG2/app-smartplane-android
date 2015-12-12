package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AchievementCheckerServiceTest extends TestCase {

    @Inject
    private AchievementCheckerService classUnderTest;
    @Mock
    private RetrofitServiceManager retrofitServiceManager;
    @Mock
    private AchievementService achievementService;
    @Mock
    private Call<List<Achievement>> call;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Set up Mockito behavior
        Mockito.when(retrofitServiceManager.getAchievmentService()).thenReturn(achievementService);

        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void testFetchAchievements() throws Exception {
        //Given
        Mockito.when(retrofitServiceManager.getAchievmentService()).thenReturn(achievementService);
        Mockito.when(achievementService.getObtainedAchievements()).thenReturn(call);
        List<Achievement> achievementList = new ArrayList<>();
        Response<List<Achievement>> response = Response.success(achievementList);
        Mockito.when(call.execute()).thenReturn(response);
        achievementList.add(new Achievement());

        //When
        List<Achievement> resultList = classUnderTest.fetchAchievements();

        //Then
        assertFalse(resultList.isEmpty());
        Mockito.verify(call).execute();
    }

    @Test
    public void testFetchAchievementsNoSuccess() throws Exception {
        //Given
        Mockito.when(retrofitServiceManager.getAchievmentService()).thenReturn(achievementService);
        Mockito.when(achievementService.getObtainedAchievements()).thenReturn(call);
        Response<List<Achievement>> response = Response.error(404, null);
        Mockito.when(call.execute()).thenReturn(response);

        //When
        List<Achievement> resultList = classUnderTest.fetchAchievements();

        //Then
        assertTrue(resultList.isEmpty());
        Mockito.verify(call).execute();
    }

    @Test
    public void testFetchAchievementsException() throws Exception {
        //Given
        Mockito.when(retrofitServiceManager.getAchievmentService()).thenReturn(achievementService);
        Mockito.when(achievementService.getObtainedAchievements()).thenReturn(call);
        Mockito.when(call.execute()).thenThrow(new IOException());

        //When
        List<Achievement> resultList = classUnderTest.fetchAchievements();

        //Then
        assertTrue(resultList.isEmpty());
        Mockito.verify(call).execute();
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(RetrofitServiceManager.class).toInstance(retrofitServiceManager);
        }
    }
}