package be.ucll.campus.campus_app.repository;

import org.springframework.data.jpa.repository.Query;
import be.ucll.campus.campus_app.model.Lokaal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LokaalRepository extends JpaRepository<Lokaal, Long> {
    List<Lokaal> findByCampusNaam(String campusNaam);
    /*@Query("SELECT l FROM Lokaal l WHERE l.id NOT IN (SELECT r.id FROM Reservatie r JOIN r.lokalen l)")
    List<Lokaal> findAvailableLokalen();*/
    Optional<Lokaal> findByIdAndCampusNaam(Long id, String campusNaam);
    /*boolean existsByNaamAndCampusNaam(String naam, String campusNaam);*/
    /*Optional<Lokaal> findByNaamAndCampus_Naam(String naam, String campusNaam);*/
    boolean existsByCampusNaamAndNaam(String campusNaam, String naam);
    @Query("SELECT l FROM Lokaal l WHERE l.campus.naam = :campusNaam AND " +
            "l.id NOT IN (SELECT rl.lokaal.id FROM ReservatieLokaal rl WHERE " +
            "(rl.reservatie.startTijd < :eindTijd AND rl.reservatie.eindTijd > :startTijd))")
    List<Lokaal> findBeschikbareLokalen(
            @Param("campusNaam") String campusNaam,
            @Param("startTijd") LocalDateTime startTijd,
            @Param("eindTijd") LocalDateTime eindTijd
    );
}


