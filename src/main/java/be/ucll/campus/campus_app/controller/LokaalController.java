
package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.model.Lokaal;
import be.ucll.campus.campus_app.model.Campus;
import be.ucll.campus.campus_app.service.LokaalService;
import be.ucll.campus.campus_app.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<Lokaal>> getLokalenByCampus(@PathVariable String campusNaam) {
        return ResponseEntity.ok(lokaalService.getLokalenByCampus(campusNaam));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lokaal> getLokaalByIdAndCampus(@PathVariable Long id, @PathVariable String campusNaam) {
        Optional<Lokaal> lokaal = lokaalService.getLokaalByIdAndCampus(id, campusNaam);
        return lokaal.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Lokaal> addLokaalToCampus(@PathVariable String campusNaam, @RequestBody Lokaal lokaal) {
        Campus campus = campusService.getCampusByNaam(campusNaam)
                .orElseThrow(() -> new RuntimeException("Campus niet gevonden: " + campusNaam));

        lokaal.setCampus(campus);
        Lokaal nieuwLokaal = lokaalService.addLokaalToCampus(lokaal);
        return ResponseEntity.ok(nieuwLokaal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLokaal(@PathVariable Long id) {
        lokaalService.deleteLokaal(id);
        return ResponseEntity.noContent().build();
    }
}
