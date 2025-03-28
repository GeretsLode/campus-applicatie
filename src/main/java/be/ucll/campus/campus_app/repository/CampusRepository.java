package be.ucll.campus.campus_app.repository;

import be.ucll.campus.campus_app.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, String> {
    Optional<Campus> findByNaam(String naam);
}
