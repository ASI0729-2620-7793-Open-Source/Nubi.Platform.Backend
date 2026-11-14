package com.acme.nubi.platform.supportnetwork.application.internal.outboundservices.acl;

import java.util.List;
import java.util.Optional;

public interface ProfilesContextFacade {

    List<ProfileData> fetchProfilesByCaregiverId(Long caregiverId);

    Optional<ProfileData> fetchProfileById(Long profileId);
}
