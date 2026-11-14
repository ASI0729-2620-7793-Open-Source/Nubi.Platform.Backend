package com.acme.nubi.platform.assistivecomunication.domain;

import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.CommunicationBoard;
import com.acme.nubi.platform.assistivecomunication.domain.model.aggregate.Pictogram;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramCategory;
import com.acme.nubi.platform.assistivecomunication.domain.model.entities.PictogramSelection;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.CategoryType;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.shared.domain.exceptions.DomainRuleViolationException;
import com.acme.nubi.platform.shared.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunicationBoardTest {

    /** Tablero con ids asignados, como si ya estuviera guardado. */
    private CommunicationBoard boardWith(int maxFavorites, int pictogramCount) {
        List<PictogramCategory> categories = List.of(
                new PictogramCategory(1L, "Necesidades básicas", CategoryType.BASIC_NEEDS, 1),
                new PictogramCategory(2L, "Emociones", CategoryType.EMOTIONS, 2));
        List<Pictogram> pictograms = new ArrayList<>();
        for (long i = 1; i <= pictogramCount; i++) {
            pictograms.add(new Pictogram(i, 1L, "P" + i, "Frase " + i, "p" + i + ".png", null,
                    false, false, (int) i));
        }
        return new CommunicationBoard(10L, new ProfileId(1L), maxFavorites, categories, pictograms);
    }

    @Test
    void markFavoriteRejectsWhenLimitIsReached() {
        CommunicationBoard board = boardWith(2, 3);
        board.markFavorite(1L);
        board.markFavorite(2L);

        assertThrows(DomainRuleViolationException.class, () -> board.markFavorite(3L));
        assertEquals(2, board.getQuickAccess().size());
    }

    @Test
    void markFavoriteIsIdempotentAndUnmarkFreesASlot() {
        CommunicationBoard board = boardWith(1, 2);
        board.markFavorite(1L);
        board.markFavorite(1L);
        board.unmarkFavorite(1L);
        board.markFavorite(2L);

        assertTrue(board.getPictogram(2L).isFavorite());
        assertFalse(board.getPictogram(1L).isFavorite());
    }

    @Test
    void moveToCategoryRejectsUnknownCategory() {
        CommunicationBoard board = boardWith(3, 1);

        assertThrows(ResourceNotFoundException.class, () -> board.moveToCategory(1L, 99L));
        board.moveToCategory(1L, 2L);
        assertEquals(2L, board.getPictogram(1L).getCategoryId());
    }

    @Test
    void addPictogramRejectsUnsupportedImageAndBlankLabel() {
        CommunicationBoard board = boardWith(3, 0);

        assertThrows(IllegalArgumentException.class, () -> board.addPictogram("Perro", null, "foto.pdf", null, 1L));
        assertThrows(IllegalArgumentException.class, () -> board.addPictogram(" ", null, "foto.png", null, 1L));
        assertThrows(ResourceNotFoundException.class, () -> board.addPictogram("Perro", null, "foto.png", null, 99L));
    }

    @Test
    void addPictogramUsesLabelAsPhraseWhenMissing() {
        CommunicationBoard board = boardWith(3, 0);

        Pictogram pictogram = board.addPictogram("Perro", null, "https://cdn.test/perro.PNG?v=2", null, 1L);

        assertEquals("Perro", pictogram.getPhrase());
        assertTrue(pictogram.isCustom());
        assertFalse(pictogram.hasAudio());
    }

    @Test
    void addCategoryRejectsDuplicatedName() {
        CommunicationBoard board = boardWith(3, 0);

        PictogramCategory category = board.addCategory("Escuela");

        assertEquals(CategoryType.CUSTOM, category.getType());
        assertThrows(DomainRuleViolationException.class, () -> board.addCategory("escuela"));
    }

    @Test
    void selectPictogramCreatesPendingSelection() {
        CommunicationBoard board = boardWith(3, 1);

        PictogramSelection selection = board.selectPictogram(1L);

        assertEquals(SelectionStatus.PENDING_DELIVERY, selection.getStatus());
        assertEquals(10L, selection.getBoardId());
        assertThrows(ResourceNotFoundException.class, () -> board.selectPictogram(99L));
    }

    @Test
    void selectionLifecycleRejectsChangesOnceResolved() {
        PictogramSelection selection = boardWith(3, 1).selectPictogram(1L);

        selection.markAsDelivered();
        selection.confirmUnderstanding();

        assertEquals(SelectionStatus.UNDERSTOOD, selection.getStatus());
        assertThrows(DomainRuleViolationException.class, selection::markAsNotUnderstood);
        assertThrows(DomainRuleViolationException.class, selection::markAsDelivered);
    }

    @Test
    void defaultBoardIsCreatedWithoutPictogramsUntilLoaded() {
        CommunicationBoard board = CommunicationBoard.createDefault(new ProfileId(5L));

        assertEquals(3, board.getCategories().size());
        assertTrue(board.getPictograms().isEmpty());
    }

    @Test
    void profileIdMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new ProfileId(0L));
        assertThrows(IllegalArgumentException.class, () -> new ProfileId(null));
    }
}
