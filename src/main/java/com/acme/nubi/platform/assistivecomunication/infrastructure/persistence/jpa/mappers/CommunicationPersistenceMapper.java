package com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.mappers;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramCategory;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.CommunicationBoardPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.MoodCheckInPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramCategoryPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramPersistenceEntity;
import com.acme.nubi.platform.assistivecomunication.infrastructure.persistence.jpa.entities.PictogramSelectionPersistenceEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convierte entre los objetos de dominio (sin anotaciones de JPA) y las entidades de persistencia.
 */
@Component
public class CommunicationPersistenceMapper {

    // ---- Tablero (aggregate) ----

    public CommunicationBoard toDomain(CommunicationBoardPersistenceEntity entity) {
        var categories = entity.getCategories().stream().map(this::toDomain).toList();
        var pictograms = entity.getPictograms().stream().map(this::toDomain).toList();
        return new CommunicationBoard(entity.getId(), entity.getProfileId(), entity.getMaxFavorites(),
                categories, pictograms);
    }

    public CommunicationBoardPersistenceEntity toNewEntity(CommunicationBoard board) {
        CommunicationBoardPersistenceEntity entity = new CommunicationBoardPersistenceEntity();
        entity.setProfileId(board.getProfileId());
        apply(board, entity);
        return entity;
    }

    /** Copia el estado del dominio sobre la entidad ya existente, conservando las filas por id. */
    public void apply(CommunicationBoard board, CommunicationBoardPersistenceEntity entity) {
        entity.setMaxFavorites(board.getMaxFavorites());
        applyCategories(board, entity);
        applyPictograms(board, entity);
    }

    private void applyCategories(CommunicationBoard board, CommunicationBoardPersistenceEntity entity) {
        Map<Long, PictogramCategoryPersistenceEntity> existing = new HashMap<>();
        entity.getCategories().forEach(category -> existing.put(category.getId(), category));

        Set<Long> keptIds = new HashSet<>();
        for (PictogramCategory category : board.getCategories()) {
            PictogramCategoryPersistenceEntity target = category.getId() == null ? null : existing.get(category.getId());
            if (target == null) {
                target = new PictogramCategoryPersistenceEntity();
                target.setBoard(entity);
                entity.getCategories().add(target);
            } else {
                keptIds.add(target.getId());
            }
            target.setName(category.getName());
            target.setType(category.getType());
            target.setDisplayOrder(category.getDisplayOrder());
        }
        entity.getCategories().removeIf(category -> category.getId() != null && !keptIds.contains(category.getId()));
    }

    private void applyPictograms(CommunicationBoard board, CommunicationBoardPersistenceEntity entity) {
        Map<Long, PictogramCategoryPersistenceEntity> categoriesById = new HashMap<>();
        entity.getCategories().stream()
                .filter(category -> category.getId() != null)
                .forEach(category -> categoriesById.put(category.getId(), category));
        Map<Long, PictogramPersistenceEntity> existing = new HashMap<>();
        entity.getPictograms().forEach(pictogram -> existing.put(pictogram.getId(), pictogram));

        Set<Long> keptIds = new HashSet<>();
        for (Pictogram pictogram : board.getPictograms()) {
            PictogramCategoryPersistenceEntity category = categoriesById.get(pictogram.getCategoryId());
            if (category == null) {
                throw new IllegalStateException("La categoría " + pictogram.getCategoryId() + " no está guardada");
            }
            PictogramPersistenceEntity target = pictogram.getId() == null ? null : existing.get(pictogram.getId());
            if (target == null) {
                target = new PictogramPersistenceEntity();
                target.setBoard(entity);
                entity.getPictograms().add(target);
            } else {
                keptIds.add(target.getId());
            }
            target.setCategory(category);
            target.setLabel(pictogram.getLabel());
            target.setPhrase(pictogram.getPhrase());
            target.setImageUrl(pictogram.getImageUrl());
            target.setAudioUrl(pictogram.getAudioUrl());
            target.setFavorite(pictogram.isFavorite());
            target.setCustom(pictogram.isCustom());
            target.setDisplayOrder(pictogram.getDisplayOrder());
        }
        entity.getPictograms().removeIf(pictogram -> pictogram.getId() != null && !keptIds.contains(pictogram.getId()));
    }

    private PictogramCategory toDomain(PictogramCategoryPersistenceEntity entity) {
        return new PictogramCategory(entity.getId(), entity.getName(), entity.getType(), entity.getDisplayOrder());
    }

    private Pictogram toDomain(PictogramPersistenceEntity entity) {
        return new Pictogram(entity.getId(), entity.getCategory().getId(), entity.getLabel(), entity.getPhrase(),
                entity.getImageUrl(), entity.getAudioUrl(), entity.isFavorite(), entity.isCustom(),
                entity.getDisplayOrder());
    }

    // ---- Selecciones ----

    public PictogramSelection toDomain(PictogramSelectionPersistenceEntity entity) {
        return new PictogramSelection(entity.getId(), entity.getBoard().getId(), entity.getPictogram().getId(),
                entity.getStatus(), entity.getSelectedAt(), entity.getUnderstoodAt());
    }

    public void apply(PictogramSelection selection, PictogramSelectionPersistenceEntity entity) {
        entity.setStatus(selection.getStatus());
        entity.setSelectedAt(selection.getSelectedAt());
        entity.setUnderstoodAt(selection.getUnderstoodAt());
    }

    // ---- Check-ins de ánimo ----

    public MoodCheckIn toDomain(MoodCheckInPersistenceEntity entity) {
        return new MoodCheckIn(entity.getId(), entity.getBoard().getId(), entity.getLevel(), entity.getCheckedAt());
    }

    public void apply(MoodCheckIn moodCheckIn, MoodCheckInPersistenceEntity entity) {
        entity.setLevel(moodCheckIn.getLevel());
        entity.setCheckedAt(moodCheckIn.getCheckedAt());
    }
}
