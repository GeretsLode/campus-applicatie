package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.model.*;
import be.ucll.campus.campus_app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/campus")
public class CampusController {

    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @Operation(summary = "Wijzigt een bestaande campus", description = "Past gegevens van een bestaande campus aan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campus succesvol bijgewerkt"),
            @ApiResponse(responseCode = "404", description = "Campus niet gevonden")
    })
    @PutMapping("/{campus-id}")
    public ResponseEntity<Campus> updateCampus(@PathVariable("campus-id") String campusId, @RequestBody Campus updatedCampus) {
        Optional<Campus> campus = campusService.updateCampus(campusId, updatedCampus);
        return campus.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Voegt een nieuwe campus toe", description = "Maakt een nieuwe campus aan met naam, adres en parkeergegevens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campus succesvol aangemaakt"),
            @ApiResponse(responseCode = "400", description = "Ongeldige inputdata")
    })
    @PostMapping
    public ResponseEntity<Campus> addCampus(@RequestBody Campus campus) {
        Campus savedCampus = campusService.addCampus(campus);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCampus);
    }

    @Operation(summary = "Geeft alle campussen", description = "Haalt een lijst op van alle campussen in het systeem.")
    @ApiResponse(responseCode = "200", description = "Lijst van campussen opgehaald")
    @GetMapping
    public List<Campus> getAllCampuses() {
        return campusService.getAllCampuses();
    }

    @Operation(summary = "Zoekt een campus op ID", description = "Geeft details terug van de campus met het opgegeven ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campus gevonden"),
            @ApiResponse(responseCode = "404", description = "Campus niet gevonden")
    })
    @GetMapping("/{campus-id}")
    public ResponseEntity<Campus> getCampusById(@PathVariable("campus-id") String id) {
        return campusService.getCampusById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Verwijdert een campus", description = "Verwijdert de campus met het opgegeven ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Campus succesvol verwijderd"),
            @ApiResponse(responseCode = "404", description = "Campus niet gevonden")
    })
    @DeleteMapping("/{campus-id}")
    public ResponseEntity<Void> deleteCampus(@PathVariable("campus-id") String id) {
        campusService.deleteCampus(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
