
package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.model.Lokaal;
import be.ucll.campus.campus_app.model.Campus;
import be.ucll.campus.campus_app.service.LokaalService;
import be.ucll.campus.campus_app.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/campus/{campusNaam}/rooms")
public class LokaalController {

    private final LokaalService lokaalService;
    private final CampusService campusService;

    @Autowired
    public LokaalController(LokaalService lokaalService, CampusService campusService) {
        this.lokaalService = lokaalService;
        this.campusService = campusService;
    }

    @Operation(summary = "Lijst van lokalen per campus", description = "Geeft een overzicht van alle lokalen binnen een opgegeven campus")
    @ApiResponse(responseCode = "200", description = "Lokalen opgehaald")
    @GetMapping
    public ResponseEntity<List<Lokaal>> getLokalenByCampus(@PathVariable String campusNaam) {
        return ResponseEntity.ok(lokaalService.getLokalenByCampus(campusNaam));
    }

    @Operation(summary = "Details van een lokaal opvragen", description = "Haalt een specifiek lokaal op via ID en campusnaam")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lokaal gevonden"),
            @ApiResponse(responseCode = "404", description = "Lokaal niet gevonden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Lokaal> getLokaalByIdAndCampus(@PathVariable Long id, @PathVariable String campusNaam) {
        Optional<Lokaal> lokaal = lokaalService.getLokaalByIdAndCampus(id, campusNaam);
        return lokaal.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Voegt een lokaal toe aan een campus", description = "Maakt een nieuw lokaal aan en koppelt het aan de opgegeven campus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lokaal succesvol aangemaakt"),
            @ApiResponse(responseCode = "404", description = "Campus niet gevonden")
    })
    @PostMapping
    public ResponseEntity<Lokaal> addLokaalToCampus(@PathVariable String campusNaam, @RequestBody Lokaal lokaal) {
        Campus campus = campusService.getCampusByNaam(campusNaam)
                .orElseThrow(() -> new RuntimeException("Campus niet gevonden: " + campusNaam));

        lokaal.setCampus(campus);
        Lokaal nieuwLokaal = lokaalService.addLokaalToCampus(lokaal);
        return ResponseEntity.ok(nieuwLokaal);
    }

    @Operation(summary = "Verwijdert een lokaal", description = "Verwijdert een lokaal op basis van ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lokaal succesvol verwijderd"),
            @ApiResponse(responseCode = "404", description = "Lokaal niet gevonden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLokaal(@PathVariable Long id) {
        lokaalService.deleteLokaal(id);
        return ResponseEntity.noContent().build();
    }
}
