package com.acme.nubi.platform.assistivecomunication.domain.model.aggregate;

import com.acme.nubi.platform.assistivecomunication.domain.model.entities.MoodCheckIn;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramCategory;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.DefaultPictogramCatalog;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.EmotionalIntensityLevel;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.PictogramTemplate;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.shared.domain.exceptions.DomainRuleViolationException;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root del tablero de Comunicación Asistida (CAA). Hay un tablero por perfil.
 * Agrupa las categorías y los pictogramas, y valida la regla de máximo de favoritos.
 * Las selecciones y los check-ins de ánimo son entidades que el tablero crea pero que se
 * guardan aparte, porque su cantidad crece sin límite.
 */
@Getter
public class CommunicationBoard {
    public static final int DEFAULT_MAX_FAVORITES = 8;

    private Long id;
    private final ProfileId profileId;
    private Integer maxFavorites;
    private final List<PictogramCategory> categories;
    private final List<Pictogram> pictograms;

    public CommunicationBoard(Long id, ProfileId profileId, Integer maxFavorites,
                              List<PictogramCategory> categories, List<Pictogram> pictograms) {
        if (profileId == null) {
            throw new IllegalArgumentException("El tablero debe pertenecer a un perfil");
        }
        if (maxFavorites == null || maxFavorites <= 0) {
            throw new IllegalArgumentException("El máximo de favoritos debe ser mayor que cero");
        }
        this.id = id;
        this.profileId = profileId;
        this.maxFavorites = maxFavorites;
        this.categories = new ArrayList<>(categories);
        this.pictograms = new ArrayList<>(pictograms);
    }

    /** Tablero nuevo con las categorías básicas. Los pictogramas por defecto se cargan con {@link #loadDefaultPictograms()}. */
    public static CommunicationBoard createDefault(ProfileId profileId) {
        List<PictogramCategory> defaults = List.of(
                new PictogramCategory("Necesidades básicas", CategoryType.BASIC_NEEDS, 1),
                new PictogramCategory("Emociones", CategoryType.EMOTIONS, 2),
                new PictogramCategory("Actividades", CategoryType.ACTIVITIES, 3));
        return new CommunicationBoard(null, profileId, DEFAULT_MAX_FAVORITES, defaults, List.of());
    }

    public List<PictogramCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public List<Pictogram> getPictograms() {
        return Collections.unmodifiableList(pictograms);
    }

    /** Agrega la categoría del cuidador (tipo CUSTOM). El nombre debe ser único dentro del tablero. */
    public PictogramCategory addCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        boolean exists = categories.stream().anyMatch(category -> category.getName().equalsIgnoreCase(name.trim()));
        if (exists) {
            throw new DomainRuleViolationException("Ya existe una categoría llamada '" + name.trim() + "' en el tablero");
        }
        int nextOrder = categories.stream().mapToInt(PictogramCategory::getDisplayOrder).max().orElse(0) + 1;
        PictogramCategory category = new PictogramCategory(name, CategoryType.CUSTOM, nextOrder);
        categories.add(category);
        return category;
    }

    /** Agrega un pictograma personalizado a una categoría existente (US-22). */
    public Pictogram addPictogram(String label, String phrase, String imageUrl, String audioUrl, Long categoryId) {
        requireCategory(categoryId);
        Pictogram pictogram = new Pictogram(null, categoryId, label, phrase, imageUrl, audioUrl,
                false, true, nextDisplayOrder(categoryId));
        pictograms.add(pictogram);
        return pictogram;
    }

    /** Carga los pictogramas básicos si el tablero aún no tiene ninguno (US-19, escenario 2). */
    public void loadDefaultPictograms() {
        if (!pictograms.isEmpty()) {
            return;
        }
        for (PictogramTemplate template : DefaultPictogramCatalog.BASIC_PICTOGRAMS) {
            PictogramCategory category = categories.stream()
                    .filter(candidate -> candidate.getType() == template.category())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "El tablero no tiene la categoría " + template.category()));
            boolean canBeFavorite = template.favorite() && countFavorites() < maxFavorites;
            pictograms.add(new Pictogram(null, category.getId(), template.label(), template.phrase(),
                    template.imageUrl(), null, canBeFavorite, false, nextDisplayOrder(category.getId())));
        }
    }

    /** Marca un pictograma como favorito (US-21). Falla si ya se alcanzó el máximo permitido. */
    public void markFavorite(Long pictogramId) {
        Pictogram pictogram = getPictogram(pictogramId);
        if (pictogram.isFavorite()) {
            return;
        }
        if (countFavorites() >= maxFavorites) {
            throw new DomainRuleViolationException(
                    "Se alcanzó el máximo de pictogramas favoritos permitido (" + maxFavorites + ")");
        }
        pictogram.markAsFavorite();
    }

    public void unmarkFavorite(Long pictogramId) {
        getPictogram(pictogramId).unmarkAsFavorite();
    }

    /** Reubica un pictograma en otra categoría del tablero (US-23). */
    public void moveToCategory(Long pictogramId, Long categoryId) {
        Pictogram pictogram = getPictogram(pictogramId);
        requireCategory(categoryId);
        pictogram.assignToCategory(categoryId);
    }

    /** El usuario toca un pictograma: se crea el mensaje pendiente de entrega (US-20). */
    public PictogramSelection selectPictogram(Long pictogramId) {
        Pictogram pictogram = getPictogram(pictogramId);
        return new PictogramSelection(id, pictogram.getId());
    }

    /** Check-in emocional con la escala de cuatro niveles. */
    public MoodCheckIn registerMood(EmotionalIntensityLevel level) {
        return new MoodCheckIn(id, level);
    }

    /** Acceso rápido: los pictogramas favoritos, en su orden de visualización. */
    public List<Pictogram> getQuickAccess() {
        return pictograms.stream()
                .filter(Pictogram::isFavorite)
                .sorted(Comparator.comparing(Pictogram::getDisplayOrder))
                .toList();
    }

    public Pictogram getPictogram(Long pictogramId) {
        return pictograms.stream()
                .filter(pictogram -> Objects.equals(pictogram.getId(), pictogramId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pictograma", pictogramId));
    }

    public PictogramCategory requireCategory(Long categoryId) {
        return categories.stream()
                .filter(category -> Objects.equals(category.getId(), categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La categoría " + categoryId + " no existe en el tablero; créala primero"));
    }

    private long countFavorites() {
        return pictograms.stream().filter(Pictogram::isFavorite).count();
    }

    private int nextDisplayOrder(Long categoryId) {
        return pictograms.stream()
                .filter(pictogram -> Objects.equals(pictogram.getCategoryId(), categoryId))
                .mapToInt(Pictogram::getDisplayOrder)
                .max()
                .orElse(0) + 1;
    }
}
