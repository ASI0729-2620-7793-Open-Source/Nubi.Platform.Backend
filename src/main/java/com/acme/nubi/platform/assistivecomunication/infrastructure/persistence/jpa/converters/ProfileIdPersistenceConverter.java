package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.converters;

import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import jakarta.persistence.AttributeConverter;

public class ProfileIdPersistenceConverter implements AttributeConverter<ProfileId, Long> {
    @Override
    public Long convertToDatabaseColumn(ProfileId attribute) {
        return attribute == null ? null : attribute.profileId();
    }

    @Override
    public ProfileId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : new ProfileId(dbData);
    }
}
