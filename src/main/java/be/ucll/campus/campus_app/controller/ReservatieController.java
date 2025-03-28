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

    @Operation(summary = "Voeg een lokaal toe aan een bestaande reservatie")
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

    @Operation(summary = "Toon reservaties voor een specifiek lokaal binnen een campus")
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getAllReservaties() {
        List<ReservatieDTO> reservatieDTOS = reservatieService.getAllReservaties() // Zorg dat dit List<Reservatie> is
                .stream()
                .map(ReservatieDTO::fromReservatie) // Expliciete lambda i.p.v. method reference
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservatieDTOS);
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservatieDTO> getReservatieById(@PathVariable Long id) {
        return reservatieService.getReservatieById(id)
                .map(ReservatieDTO::fromReservatie)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getReservatiesByUser(@PathVariable Long userId) {
        List<ReservatieDTO> dtos = reservatieService.getReservatiesByUserId(userId).stream()
                .map(ReservatieDTO::fromReservatie)
                .toList();
        dtos.forEach(dto -> System.out.println("DTO: " + dto.getId() + " campus = " + dto.getCampusNaam()));
        return ResponseEntity.ok(dtos);
    }

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

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteReservatie(@PathVariable Long id) {
        reservatieService.deleteReservatie(id);
        return ResponseEntity.noContent().build();
    }

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
    @GetMapping(value = "/per-lokaal-campus", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservatieDTO>> getReservatiesVoorCampus(@RequestParam String campusNaam) {
        List<Reservatie> reservaties = reservatieService.getReservatiesVoorCampus(campusNaam);
        List<ReservatieDTO> dtos = reservaties.stream()
                .map(ReservatieDTO::fromReservatie)
                .toList();
        return ResponseEntity.ok(dtos);
    }

}
