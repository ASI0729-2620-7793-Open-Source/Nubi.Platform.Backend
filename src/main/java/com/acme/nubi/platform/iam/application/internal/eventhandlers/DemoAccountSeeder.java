package com.acme.nubi.platform.iam.application.internal.eventhandlers;

import com.acme.nubi.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.nubi.platform.iam.domain.repositories.UserAccountRepository;
import com.acme.nubi.platform.iam.domain.services.AuthenticationCommandService;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.CreateProfileRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateDiagnosisRequest;
import com.acme.nubi.platform.profile.application.dto.ProfileRequests.UpdateSensoryProfileRequest;
import com.acme.nubi.platform.profile.application.services.ProfileApplicationService;
import com.acme.nubi.platform.profile.domain.model.enums.CommunicativeNeed;
import com.acme.nubi.platform.profile.domain.model.enums.ConditionType;
import com.acme.nubi.platform.profile.domain.model.enums.Gender;
import com.acme.nubi.platform.profile.domain.model.enums.SensitivityLevel;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Crea una cuenta de demostración con un niño para probar Nubi apenas arranca el backend:
 * {@code demo@nubi.app} / {@code nubi123}. Se desactiva con {@code nubi.demo.enabled=false}.
 */
@Service("iamDemoAccountSeeder")
public class DemoAccountSeeder {

    public static final String DEMO_EMAIL = "demo@nubi.app";
    public static final String DEMO_PASSWORD = "nubi123";

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoAccountSeeder.class);

    private final AuthenticationCommandService authenticationCommandService;
    private final UserAccountRepository userAccountRepository;
    private final ProfileApplicationService profileApplicationService;
    private final boolean enabled;

    public DemoAccountSeeder(AuthenticationCommandService authenticationCommandService,
                             UserAccountRepository userAccountRepository,
                             ProfileApplicationService profileApplicationService,
                             @Value("${nubi.demo.enabled:true}") boolean enabled) {
        this.authenticationCommandService = authenticationCommandService;
        this.userAccountRepository = userAccountRepository;
        this.profileApplicationService = profileApplicationService;
        this.enabled = enabled;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        if (!enabled) {
            return;
        }
        var account = userAccountRepository.findByEmail(DEMO_EMAIL)
                .or(() -> authenticationCommandService
                        .handle(new SignUpCommand(DEMO_EMAIL, "Ana Ríos", DEMO_PASSWORD))
                        .success());
        if (account.isEmpty()) {
            return;
        }
        var accountId = account.get().getId();
        if (!profileApplicationService.listOwned(accountId).isEmpty()) {
            return;
        }
        profileApplicationService.create(accountId, new CreateProfileRequest(
                        "Mateo", "Ríos", "Mati", 7, Gender.MALE, CommunicativeNeed.PICTOGRAMS, DEMO_EMAIL))
                .success()
                .ifPresent(profile -> {
                    profileApplicationService.updateSensitivities(accountId, profile.id(),
                            new UpdateSensoryProfileRequest(SensitivityLevel.HIGH, SensitivityLevel.MEDIUM,
                                    SensitivityLevel.HIGH, false, true, false));
                    profileApplicationService.updateDiagnosis(accountId, profile.id(),
                            new UpdateDiagnosisRequest(ConditionType.ASD, null, "Dra. Lucía Paredes",
                                    LocalDate.of(2023, 3, 14), "TEA nivel 1 de apoyo. Le calman el agua y las luces suaves."));
                    LOGGER.info("Demo account {} ready with profile {}", DEMO_EMAIL, profile.id());
                });
    }
}
