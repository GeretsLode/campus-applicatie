// UserRepository.java
package be.ucll.campus.campus_app.repository;

import be.ucll.campus.campus_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNaamContainingIgnoreCaseOrVoornaamContainingIgnoreCase(String naam, String voornaam);
}