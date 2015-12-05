package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.dispatcher.event.AchievementUnlockedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public void startAchievementMonitoring(){
        //TODO: Martin kannst du das fixen?
        //Thread policy musste umgebogen werden, da internetzugriff im Mainthread passiert
        //siehe: http://stackoverflow.com/questions/6976317/android-http-connection-exception/6986726#6986726
        //und http://stackoverflow.com/questions/22395417/error-strictmodeandroidblockguardpolicy-onnetwork
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        handler = new Handler();
        handler.postDelayed(runnable, DELAY);
    }

    public void stopAchievementMonitoring(){
        handler.removeCallbacks(runnable);
    }

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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Get achievements
            newAchievements = fetchAchievements();
            tmpAchievements = newAchievements;

            //Find new ones
            newAchievements.remove(currentAchievements);

            //Create event if there is more than 0 new achievement
            //current achievement initialized (not first run)
            if (newAchievements.size() > 0 && !currentAchievements.equals(null)){
                EventBus.getDefault().post(new AchievementUnlockedEvent(newAchievements));
            }

            //save achievements
            currentAchievements = tmpAchievements;

            //do it again (loop)
            handler.postDelayed(this, DELAY);
        }
    };

}
