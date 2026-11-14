package com.acme.nubi.platform.supportnetwork.application.internal.outboundservices.acl;

import java.util.List;

public interface AlertsContextFacade {

    List<AlertData> fetchRecentAlertsByProfileId(Long profileId, Integer limit);
}
