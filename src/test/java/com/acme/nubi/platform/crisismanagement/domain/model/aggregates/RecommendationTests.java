package com.acme.nubi.platform.crisismanagement.domain.model.aggregates;

import com.acme.nubi.platform.crisismanagement.domain.model.policies.RecommendationGenerator;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.AccountId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.ProfileId;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.RecommendationTopic;
import com.acme.nubi.platform.crisismanagement.domain.model.valueobjects.SensoryTrigger;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecommendationTests {

    private static final Instant NOW = Instant.parse("2026-11-12T10:00:00Z");

    private Recommendation recommendation() {
        return new Recommendation(new ProfileId(1L), RecommendationTopic.SENSORY, "Título", "Contenido", NOW);
    }

    @Test
    void startsAsTheCurrentFirstVersion() {
        var recommendation = recommendation();

        assertThat(recommendation.getVersion()).isEqualTo(1);
        assertThat(recommendation.isOutdated()).isFalse();
    }

    @Test
    void replacingItMarksTheOldVersionAsOutdatedAndReturnsTheNext() {
        var old = recommendation();

        var next = old.replaceWith("Contenido nuevo", NOW.plusSeconds(60));

        assertThat(old.isOutdated()).isTrue();
        assertThat(next.isOutdated()).isFalse();
        assertThat(next.getVersion()).isEqualTo(2);
        assertThat(next.getContent()).isEqualTo("Contenido nuevo");
        assertThat(next.getTopic()).isEqualTo(old.getTopic());
        assertThat(next.getPreferences()).isEmpty();
    }

    @Test
    void ratingTwiceUpdatesTheSamePreferenceOfTheCaregiver() {
        var recommendation = recommendation();
        var caregiver = new AccountId(7L);

        recommendation.ratePreference(caregiver, 4, null);
        recommendation.ratePreference(caregiver, null, true);
        recommendation.ratePreference(caregiver, 2, null);

        assertThat(recommendation.getPreferences()).singleElement().satisfies(preference -> {
            assertThat(preference.getUsefulnessRating()).isEqualTo(2);
            assertThat(preference.isFavorite()).isTrue();
        });
    }

    @Test
    void keepsOnePreferencePerCaregiver() {
        var recommendation = recommendation();

        recommendation.ratePreference(new AccountId(7L), 5, null);
        recommendation.ratePreference(new AccountId(8L), 1, null);

        assertThat(recommendation.getPreferences()).hasSize(2);
    }

    @Test
    void rejectsRatingsOutsideOneToFive() {
        var recommendation = recommendation();

        assertThatThrownBy(() -> recommendation.ratePreference(new AccountId(7L), 0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recommendation.ratePreference(new AccountId(7L), 6, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsARatingThatChangesNothing() {
        assertThatThrownBy(() -> recommendation().ratePreference(new AccountId(7L), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anOutdatedVersionCannotBeRated() {
        var recommendation = recommendation();
        recommendation.markAsOutdated();

        assertThatThrownBy(() -> recommendation.ratePreference(new AccountId(7L), 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generatesOneRecommendationPerTopicAdjustedToTheTrigger() {
        var generated = RecommendationGenerator.generateFor(new ProfileId(1L), SensoryTrigger.AUDITORY, NOW);

        assertThat(generated).extracting(Recommendation::getTopic)
                .containsExactly(RecommendationTopic.SENSORY, RecommendationTopic.COMMUNICATIONAL,
                        RecommendationTopic.BEHAVIORAL);
        assertThat(generated.getFirst().getContent()).contains("sonidos");
    }

    @Test
    void asksToCompleteTheProfileWhenThereIsNoTrigger() {
        var generated = RecommendationGenerator.generateFor(new ProfileId(1L), SensoryTrigger.NONE, NOW);

        assertThat(generated.getFirst().getContent()).contains("Completa el perfil sensorial");
    }
}
