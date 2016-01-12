package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.AsyncTask;
import android.util.Log;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.dispatcher.event.AchievementUnlockedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

public class AchievementCheckerService {

    public static final int DELAY = 15;
    @Inject
    private RetrofitServiceManager retrofitServiceManager;
    private List<Achievement> currentAchievements;
    private ScheduledExecutorService executor;
    /**
     * Runnable fetch new achievements and compare them to the previous ones
     * if new achievement present, create event to notify activity
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Get achievements and clone list to save
            List<Achievement> newAchievements = fetchAchievements();
            List<Achievement> tmpAchievements = new ArrayList<>(newAchievements);

            //Find new ones by removing all previous achievements
            newAchievements.removeAll(currentAchievements);

            //Create event if there is more than 0 new achievement
            //current achievement initialized (not first run)
            if (!newAchievements.isEmpty()) {
                //save achievements for next check
                currentAchievements = new ArrayList<>(tmpAchievements);
                EventBus.getDefault().post(new AchievementUnlockedEvent(newAchievements));
            }
        }
    };

    /**
     * Starts periodical check for new Achievements
     */
    public void startAchievementMonitoring(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                currentAchievements = fetchAchievements();
                executor = Executors.newScheduledThreadPool(1);
                executor.scheduleAtFixedRate(runnable, DELAY, DELAY, TimeUnit.SECONDS);
                return null;
            }
        }.execute((Void) null);
    }

    public void stopAchievementMonitoring(){
        executor.shutdown();
    }

    /**
     * Returns the list of obtained from server
     */
    /* package */List<Achievement> fetchAchievements() {
        final AchievementService achievementService;
        List<Achievement> achievementList = new ArrayList<>();
        try {
            achievementService = retrofitServiceManager.getAchievmentService();
            Call<List<Achievement>> call = achievementService.getObtainedAchievements();
            Response<List<Achievement>> response = call.execute();
            if (response.isSuccess()) {
                achievementList = response.body();
                Log.d(this.getClass().getSimpleName(), "Checked for new achievements from server");
            } else {
                Log.w(this.getClass().getSimpleName(), "Response no success, error code: " + response.code());
            }
        } catch (IOException e) {
            Log.w(this.getClass().getSimpleName(), "Unable to fetch Achievements");
        }
        return achievementList;
    }
}