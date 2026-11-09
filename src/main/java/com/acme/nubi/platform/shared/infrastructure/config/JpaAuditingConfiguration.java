package com.acme.nubi.platform.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita la auditoría de JPA (createdAt / updatedAt) sobre las entidades
 * que heredan de AuditableAbstractPersistenceEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
