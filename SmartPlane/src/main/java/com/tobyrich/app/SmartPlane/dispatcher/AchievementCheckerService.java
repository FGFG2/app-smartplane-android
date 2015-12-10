package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.Handler;
import android.util.Log;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.dispatcher.event.AchievementUnlockedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by anon on 01.12.2015.
 */
public class AchievementCheckerService {

    @Inject
    private RetrofitServiceManager retrofitServiceManager;

    private Handler handler;
    public static final int DELAY = 30000;
    private List<Achievement> currentAchievements;
    private List<Achievement> newAchievements;
    private List<Achievement> tmpAchievements;

    private ExecutorService executor;


    /**
     * Starts the periodically check for new Achievements
     */
    public void startAchievementMonitoring(){
        executor = Executors.newFixedThreadPool(1);
        executor.execute(runnable);
    }

    public void stopAchievementMonitoring(){
        executor.shutdown();
        executor.shutdownNow();
    }

    /**
     * Returns the list of obtained from server
     * **/
    public List<Achievement> fetchAchievements(){
        final AchievementService achievementService;
        List<Achievement> achievementList = new ArrayList<Achievement>();
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

    /**
     * Runnable methode which fetches new achievements and compares them to the previous run
     * if new achievement appears, create event for event bus
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                //Get achievements
                newAchievements = fetchAchievements();
                tmpAchievements = new ArrayList<Achievement>(newAchievements);

                //Find new ones
                newAchievements.remove(currentAchievements);

                //Create event if there is more than 0 new achievement
                //current achievement initialized (not first run)
                if (newAchievements.size() > 0 && !currentAchievements.equals(null)) {
                    EventBus.getDefault().post(new AchievementUnlockedEvent(newAchievements));
                }

                //save achievements
                currentAchievements = new ArrayList<Achievement>(tmpAchievements);

                //wait delay time before xhecking again
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
