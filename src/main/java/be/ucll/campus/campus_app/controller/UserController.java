
// UserController.java
package be.ucll.campus.campus_app.controller;

import be.ucll.campus.campus_app.dto.KlantDTO;
import be.ucll.campus.campus_app.dto.KlantRequestDTO;
import be.ucll.campus.campus_app.model.User;
import be.ucll.campus.campus_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "Gebruikers", description = "Endpoints voor gebruikersbeheer")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<KlantDTO>> getUsers(@RequestParam(required = false) String search) {
        List<User> users = (search == null || search.isBlank())
                ? userService.getAllUsers()
                : userService.searchUsersByNaam(search);

        List<KlantDTO> dtos = users.stream()
                .map(KlantDTO::fromUser)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KlantDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user
                .map(KlantDTO::fromUser)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Maak een nieuwe gebruiker aan", description = "Voegt een nieuwe gebruiker toe aan de database")
    @PostMapping
    public ResponseEntity<KlantDTO> createUser(@RequestBody KlantRequestDTO dto) {
        User newUser = userService.createUser(dto);
        return ResponseEntity.ok(KlantDTO.fromUser(newUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.userExists(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gebruiker niet gevonden");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}

