package com.acme.nubi.platform.supportnetwork.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.supportnetwork.domain.model.aggregates.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    List<HelpRequest> findAllByProfileId(Long profileId);
}
