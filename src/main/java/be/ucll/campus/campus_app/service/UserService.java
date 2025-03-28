
// UserService.java
package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.dto.KlantRequestDTO;
import be.ucll.campus.campus_app.model.User;
import be.ucll.campus.campus_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(KlantRequestDTO dto) {
        User user = new User();
        user.setNaam(dto.getNaam());
        user.setVoornaam(dto.getVoornaam());
        user.setEmail(dto.getEmail());
        user.setGeboortedatum(dto.getGeboortedatum());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean userExists(Long id) {
        return userRepository.existsById(id);
    }

    public List<User> searchUsersByNaam(String zoekterm) {
        return userRepository.findByNaamContainingIgnoreCaseOrVoornaamContainingIgnoreCase(zoekterm, zoekterm);
    }

}
