package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

@Singleton
public class RetrofitServiceManager {

    private Optional<AchievementService> achievementServiceOptional = Optional.absent();

    @Inject
    private ConnectionManager connectionManager;

    public AchievementService getAchievmentService() {
        if (!achievementServiceOptional.isPresent()) {
            achievementServiceOptional = Optional.fromNullable(connectionManager.getRetrofitConnection().create(AchievementService.class));
        }
        return achievementServiceOptional.get();
    }
}
