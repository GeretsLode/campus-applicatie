package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.model.*;
import be.ucll.campus.campus_app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/campus")
public class CampusController {

    private final CampusService campusService;

    public CampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @PutMapping("/{campus-id}")
    public ResponseEntity<Campus> updateCampus(@PathVariable("campus-id") String campusId, @RequestBody Campus updatedCampus) {
        Optional<Campus> campus = campusService.updateCampus(campusId, updatedCampus);
        return campus.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Campus> addCampus(@RequestBody Campus campus) {
        Campus savedCampus = campusService.addCampus(campus);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCampus);
    }

    @GetMapping
    public List<Campus> getAllCampuses() {
        return campusService.getAllCampuses();
    }

    @GetMapping("/{campus-id}")
    public ResponseEntity<Campus> getCampusById(@PathVariable("campus-id") String id) {
        return campusService.getCampusById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{campus-id}")
    public ResponseEntity<Void> deleteCampus(@PathVariable("campus-id") String id) {
        campusService.deleteCampus(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
