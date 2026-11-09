package com.acme.nubi.platform.assistivecomunication.interfaces.rest.controllers;

import com.acme.nubi.platform.assistivecomunication.domain.model.commands.CreateCategoryCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MarkFavoriteCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.MoveToCategoryCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.commands.UnmarkFavoriteCommand;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetBoardByProfileIdQuery;
import com.acme.nubi.platform.assistivecomunication.domain.model.queries.GetQuickAccessQuery;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardCommandService;
import com.acme.nubi.platform.assistivecomunication.domain.services.CommunicationBoardQueryService;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.BoardResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.CategoryResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.CreateCategoryResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.CreatePictogramResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.MoveToCategoryResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources.PictogramResource;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.BoardResourceFromEntityAssembler;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.CategoryResourceFromEntityAssembler;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.CreatePictogramCommandFromResourceAssembler;
import com.acme.nubi.platform.assistivecomunication.interfaces.rest.transform.PictogramResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Tablero de pictogramas de un perfil: categorías, pictogramas y favoritos. */
@RestController
@RequestMapping(value = "/api/v1/profiles/{profileId}/communication-board", produces = "application/json")
@Tag(name = "Communication Board", description = "Tablero de Comunicación Asistida (CAA)")
public class CommunicationBoardController {
    private final CommunicationBoardCommandService commandService;
    private final CommunicationBoardQueryService queryService;

    public CommunicationBoardController(CommunicationBoardCommandService commandService,
                                        CommunicationBoardQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Obtiene el tablero del perfil; si no existe, lo crea con los pictogramas básicos")
    public ResponseEntity<BoardResource> getBoard(@PathVariable Long profileId) {
        var board = queryService.handle(new GetBoardByProfileIdQuery(profileId));
        return ResponseEntity.ok(BoardResourceFromEntityAssembler.toResourceFromEntity(board));
    }

    @GetMapping("/quick-access")
    @Operation(summary = "Lista los pictogramas favoritos del acceso rápido")
    public ResponseEntity<List<PictogramResource>> getQuickAccess(@PathVariable Long profileId) {
        var pictograms = queryService.handle(new GetQuickAccessQuery(profileId));
        return ResponseEntity.ok(pictograms.stream()
                .map(PictogramResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    @PostMapping("/categories")
    @Operation(summary = "Crea una categoría personalizada")
    public ResponseEntity<CategoryResource> createCategory(@PathVariable Long profileId,
                                                           @Valid @RequestBody CreateCategoryResource resource) {
        var category = commandService.handle(new CreateCategoryCommand(profileId, resource.name()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResourceFromEntityAssembler.toResourceFromEntity(category));
    }

    @PostMapping("/pictograms")
    @Operation(summary = "Agrega un pictograma personalizado a una categoría")
    public ResponseEntity<PictogramResource> createPictogram(@PathVariable Long profileId,
                                                             @Valid @RequestBody CreatePictogramResource resource) {
        var pictogram = commandService.handle(
                CreatePictogramCommandFromResourceAssembler.toCommandFromResource(profileId, resource));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PictogramResourceFromEntityAssembler.toResourceFromEntity(pictogram));
    }

    @PutMapping("/pictograms/{pictogramId}/favorite")
    @Operation(summary = "Marca un pictograma como favorito")
    public ResponseEntity<PictogramResource> markFavorite(@PathVariable Long profileId,
                                                          @PathVariable Long pictogramId) {
        var pictogram = commandService.handle(new MarkFavoriteCommand(profileId, pictogramId));
        return ResponseEntity.ok(PictogramResourceFromEntityAssembler.toResourceFromEntity(pictogram));
    }

    @DeleteMapping("/pictograms/{pictogramId}/favorite")
    @Operation(summary = "Quita un pictograma de los favoritos")
    public ResponseEntity<PictogramResource> unmarkFavorite(@PathVariable Long profileId,
                                                            @PathVariable Long pictogramId) {
        var pictogram = commandService.handle(new UnmarkFavoriteCommand(profileId, pictogramId));
        return ResponseEntity.ok(PictogramResourceFromEntityAssembler.toResourceFromEntity(pictogram));
    }

    @PutMapping("/pictograms/{pictogramId}/category")
    @Operation(summary = "Mueve un pictograma a otra categoría del tablero")
    public ResponseEntity<PictogramResource> moveToCategory(@PathVariable Long profileId,
                                                            @PathVariable Long pictogramId,
                                                            @Valid @RequestBody MoveToCategoryResource resource) {
        var pictogram = commandService.handle(
                new MoveToCategoryCommand(profileId, pictogramId, resource.categoryId()));
        return ResponseEntity.ok(PictogramResourceFromEntityAssembler.toResourceFromEntity(pictogram));
    }
}
