package com.acme.nubi.platform.assistivecomunication.interfaces.rest.controllers;

import com.acme.nubi.platform.assistivecomunication.domain.model.commands.ConfirmUnderstandingCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkNotUnderstoodCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkSelectionDeliveredCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.SelectPictogramCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetSelectionsByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.valueobjects.SelectionStatus;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardCommandService;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardQueryService;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.SelectionResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.SelectionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Mensajes que el usuario envía al seleccionar pictogramas y su confirmación de comprensión. */
@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/communication-board", produces = "application/json")
@Tag(name = "Pictogram Selections", description = "Selección de pictogramas y confirmación del acompañante")
public class PictogramSelectionController {
    private final CommunicationBoardCommandService commandService;
    private final CommunicationBoardQueryService queryService;

    public PictogramSelectionController(CommunicationBoardCommandService commandService,
                                        CommunicationBoardQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/pictograms/{pictogramId}/selections")
    @Operation(summary = "El usuario selecciona un pictograma para comunicar una necesidad")
    public ResponseEntity<SelectionResource> selectPictogram(@PathVariable Long profileId,
                                                             @PathVariable Long pictogramId) {
        var selection = commandService.handle(new SelectPictogramCommand(profileId, pictogramId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SelectionResourceFromEntityAssembler.toResourceFromEntity(selection));
    }

    @GetMapping("/selections")
    @Operation(summary = "Historial de selecciones; se puede filtrar por estado")
    public ResponseEntity<List<SelectionResource>> getSelections(@PathVariable Long profileId,
                                                                 @RequestParam(required = false) SelectionStatus status) {
        var selections = queryService.handle(new GetSelectionsByProfileIdQuery(profileId, status));
        return ResponseEntity.ok(selections.stream()
                .map(SelectionResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    @PatchMapping("/selections/{selectionId}/delivered")
    @Operation(summary = "Marca la selección como entregada al cuidador")
    public ResponseEntity<SelectionResource> markDelivered(@PathVariable Long profileId,
                                                           @PathVariable Long selectionId) {
        var selection = commandService.handle(new MarkSelectionDeliveredCommand(profileId, selectionId));
        return ResponseEntity.ok(SelectionResourceFromEntityAssembler.toResourceFromEntity(selection));
    }

    @PatchMapping("/selections/{selectionId}/understood")
    @Operation(summary = "El acompañante confirma que entendió el mensaje")
    public ResponseEntity<SelectionResource> confirmUnderstanding(@PathVariable Long profileId,
                                                                  @PathVariable Long selectionId) {
        var selection = commandService.handle(new ConfirmUnderstandingCommand(profileId, selectionId));
        return ResponseEntity.ok(SelectionResourceFromEntityAssembler.toResourceFromEntity(selection));
    }

    @PatchMapping("/selections/{selectionId}/not-understood")
    @Operation(summary = "El acompañante no confirmó la comprensión: genera una solicitud de ayuda")
    public ResponseEntity<SelectionResource> markNotUnderstood(@PathVariable Long profileId,
                                                               @PathVariable Long selectionId) {
        var selection = commandService.handle(new MarkNotUnderstoodCommand(profileId, selectionId));
        return ResponseEntity.ok(SelectionResourceFromEntityAssembler.toResourceFromEntity(selection));
    }
}
