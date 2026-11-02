package co.edu.unicauca.bancopreguntas.questions.controller;

import co.edu.unicauca.bancopreguntas.questions.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalogos")
@Tag(name = "Catálogos", description = "Catálogos normalizados de Competencias, Temas y Subtemas Saber Pro")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/competencias")
    @Operation(summary = "Obtener catálogo oficial de competencias Saber Pro")
    public ResponseEntity<List<String>> listarCompetencias() {
        return ResponseEntity.ok(catalogService.listarCompetencias());
    }

    @GetMapping("/temas")
    @Operation(summary = "Obtener catálogo de temas")
    public ResponseEntity<List<String>> listarTemas() {
        return ResponseEntity.ok(catalogService.listarTemas());
    }

    @GetMapping("/subtemas")
    @Operation(summary = "Obtener catálogo de subtemas")
    public ResponseEntity<List<String>> listarSubtemas() {
        return ResponseEntity.ok(catalogService.listarSubtemas());
    }
}

