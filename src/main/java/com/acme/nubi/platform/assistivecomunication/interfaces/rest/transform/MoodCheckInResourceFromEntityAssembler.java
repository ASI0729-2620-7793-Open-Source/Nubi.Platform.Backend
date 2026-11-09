package com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.MoodCheckInResource;

public class MoodCheckInResourceFromEntityAssembler {
    private MoodCheckInResourceFromEntityAssembler() {
    }

    public static MoodCheckInResource toResourceFromEntity(MoodCheckIn moodCheckIn) {
        return new MoodCheckInResource(moodCheckIn.getId(), moodCheckIn.getLevel().name(), moodCheckIn.getCheckedAt());
    }
}
