package com.tobyrich.app.SmartPlane.dispatcher.event;

import com.tobyrich.app.SmartPlane.api.model.Achievement;

import java.util.List;

/**
 * Created by anon on 05.12.2015.
 */
public class AchievementUnlockedEvent {
    private List<Achievement> newAchievements;

    public AchievementUnlockedEvent(List<Achievement> achievement) {
        this.newAchievements = achievement;
    }

    public List<Achievement> getNewAchievements(){
        return newAchievements;
    }
}
