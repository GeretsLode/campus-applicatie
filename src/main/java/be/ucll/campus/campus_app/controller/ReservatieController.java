package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.dto.LokaalDTO;
import be.ucll.campus.campus_app.dto.ReservatieDTO;
import be.ucll.campus.campus_app.dto.ReservatieRequestDTO;
import be.ucll.campus.campus_app.model.Reservatie;
import be.ucll.campus.campus_app.service.ReservatieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/reservaties")
@Tag(name = "Reservaties", description = "Beheer van reservaties")
public class ReservatieController {

    private final ReservatieService reservatieService;

    @Autowired
    public ReservatieController(ReservatieService reservatieService) {
        this.reservatieService = reservatieService;
    }

    @Operation(
            summary = "Voegt een lokaal toe aan een bestaande reservatie",
            description = "Voegt een extra lokaal toe aan een reservatie als deze nog beschikbaar is binnen het tijdsinterval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lokaal succesvol toegevoegd aan reservatie"),
            @ApiResponse(responseCode = "404", description = "Reservatie, gebruiker of lokaal niet gevonden"),
            @ApiResponse(responseCode = "400", description = "Lokaal reeds gekoppeld of overlapt met bestaande reservatie")
    })
    @PutMapping("/user/{userId}/reservations/{reservatieId}/rooms/{roomId}")
    public ResponseEntity<ReservatieDTO> voegLokaalToeAanReservatie(
            @PathVariable Long userId,
            @PathVariable Long reservatieId,
            @PathVariable Long roomId) {

        Optional<Reservatie> result = reservatieService.voegLokaalToeAanReservatie(userId, reservatieId, roomId);
        return result
                .map(ReservatieDTO::fromReservatie)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Toont reservaties voor een lokaal",
            description = "Haalt alle reservaties op voor een specifiek lokaal binnen een opgegeven campus"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservaties succesvol opgehaald"),
            @ApiResponse(responseCode = "404", description = "Lokaal of campus niet gevonden")
    })
    @GetMapping("/campus/{campusNaam}/rooms/{roomId}/reservations")
    public ResponseEntity<List<ReservatieDTO>> getReservatiesVoorLokaal(
            @PathVariable String campusNaam,
            @PathVariable Long roomId) {

        List<Reservatie> reservaties = reservatieService.getReservatiesVoorLokaal(campusNaam, roomId);
        List<ReservatieDTO> dtos = reservaties.stream()
                .map(ReservatieDTO::fromReservatie)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Alle reservaties ophalen")
    @ApiResponse(responseCode = "200", description = "Lijst van reservaties opgehaald")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getAllReservaties() {
        List<ReservatieDTO> reservatieDTOS = reservatieService.getAllReservaties() // Zorg dat dit List<Reservatie> is
                .stream()
                .map(ReservatieDTO::fromReservatie) // Expliciete lambda i.p.v. method reference
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservatieDTOS);
    }

    @Operation(summary = "Reservatie op ID ophalen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservatie gevonden"),
            @ApiResponse(responseCode = "404", description = "Reservatie niet gevonden")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservatieDTO> getReservatieById(@PathVariable Long id) {
        return reservatieService.getReservatieById(id)
                .map(ReservatieDTO::fromReservatie)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Reservaties van een gebruiker ophalen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lijst van reservaties opgehaald"),
            @ApiResponse(responseCode = "404", description = "Gebruiker niet gevonden")
    })
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getReservatiesByUser(@PathVariable Long userId) {
        List<ReservatieDTO> dtos = reservatieService.getReservatiesByUserId(userId).stream()
                .map(ReservatieDTO::fromReservatie)
                .toList();
        dtos.forEach(dto -> System.out.println("DTO: " + dto.getId() + " campus = " + dto.getCampusNaam()));
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Nieuwe reservatie aanmaken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservatie succesvol aangemaakt"),
            @ApiResponse(responseCode = "400", description = "Inputdata ongeldig of tijd in het verleden")
    })
    @PostMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservatieDTO> addReservatie(@PathVariable Long userId,
                                                       @RequestBody ReservatieRequestDTO dto,
                                                       @RequestParam Set<Long> lokaalIds) {
        Reservatie nieuweReservatie = reservatieService.addReservatie(
                userId,
                dto.getStartTijd(),
                dto.getEindTijd(),
                lokaalIds);
        nieuweReservatie.setCommentaar(dto.getCommentaar());

        return ResponseEntity.ok(ReservatieDTO.fromReservatie(nieuweReservatie));
    }

    @Operation(summary = "Reservatie verwijderen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservatie verwijderd"),
            @ApiResponse(responseCode = "404", description = "Reservatie niet gevonden")
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteReservatie(@PathVariable Long id) {
        reservatieService.deleteReservatie(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Beschikbare lokalen opvragen",
            description = "Geeft een lijst van beschikbare lokalen binnen een campus en tijdsinterval. Kan filteren op minimumcapaciteit en reservatie-uitbreiding."
    )
    @ApiResponse(responseCode = "200", description = "Beschikbare lokalen succesvol opgehaald")
    @GetMapping("/beschikbare-lokalen")
    public ResponseEntity<List<LokaalDTO>> getBeschikbareLokalen(
            @RequestParam String campusNaam,
            @RequestParam String startTijd,
            @RequestParam String eindTijd,
            @RequestParam(required = false) Integer minAantalPersonen,
            @RequestParam(required = false) Long reservatieId) {
            List<LokaalDTO> result = (reservatieId != null)
                ? reservatieService.findBeschikbareLokalen(campusNaam, startTijd, eindTijd, minAantalPersonen, reservatieId)
                : reservatieService.findBeschikbareLokalen(campusNaam, startTijd, eindTijd, minAantalPersonen);

        return ResponseEntity.ok(result);

    }

    @Operation(
            summary = "Reservaties voor een campus opvragen",
            description = "Geeft alle reservaties binnen een bepaalde campus"
    )
    @ApiResponse(responseCode = "200", description = "Reservaties succesvol opgehaald")
    @GetMapping(value = "/per-lokaal-campus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getReservatiesVoorCampus(@RequestParam String campusNaam) {
        List<Reservatie> reservaties = reservatieService.getReservatiesVoorCampus(campusNaam);
        List<ReservatieDTO> dtos = reservaties.stream()
                .map(ReservatieDTO::fromReservatie)
                .toList();
        return ResponseEntity.ok(dtos);
    }

}
