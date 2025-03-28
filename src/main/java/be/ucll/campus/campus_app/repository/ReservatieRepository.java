
package be.ucll.campus.campus_app.repository;

import be.ucll.campus.campus_app.model.Lokaal;
import be.ucll.campus.campus_app.model.Reservatie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface ReservatieRepository extends JpaRepository<Reservatie, Long> {
    @Query("SELECT DISTINCT r FROM Reservatie r " +
            "LEFT JOIN FETCH r.lokalen rl " +
            "LEFT JOIN FETCH rl.lokaal l " +
            "LEFT JOIN FETCH l.campus " +
            "WHERE r.gebruiker.id = :gebruikerId")
    List<Reservatie> findByGebruikerId(@Param("gebruikerId") Long gebruikerId);


    @Query("SELECT COUNT(rl) > 0 FROM ReservatieLokaal rl " +
            "JOIN rl.reservatie r " +
            "WHERE rl.lokaal.id = :lokaalId " +
            "AND ((r.startTijd BETWEEN :startTijd AND :eindTijd) " +
            "OR (r.eindTijd BETWEEN :startTijd AND :eindTijd) " +
            "OR (:startTijd BETWEEN r.startTijd AND r.eindTijd) " +
            "OR (:eindTijd BETWEEN r.startTijd AND r.eindTijd))")
    boolean existsByLokaalIdAndOverlappingTijd(@Param("lokaalId") Long lokaalId,
                                               @Param("startTijd") LocalDateTime startTijd,
                                               @Param("eindTijd") LocalDateTime eindTijd);

    @Query("SELECT COUNT(rl) > 0 FROM ReservatieLokaal rl " +
            "JOIN rl.reservatie r " +
            "WHERE rl.lokaal.id = :lokaalId " +
            "AND r.id <> :reservatieId " +
            "AND ((r.startTijd BETWEEN :startTijd AND :eindTijd) " +
            "OR (r.eindTijd BETWEEN :startTijd AND :eindTijd) " +
            "OR (:startTijd BETWEEN r.startTijd AND r.eindTijd) " +
            "OR (:eindTijd BETWEEN r.startTijd AND r.eindTijd))")
    boolean existsOverlappingExceptOwn(@Param("lokaalId") Long lokaalId,
                                       @Param("startTijd") LocalDateTime startTijd,
                                       @Param("eindTijd") LocalDateTime eindTijd,
                                       @Param("reservatieId") Long reservatieId);

    @Query("SELECT DISTINCT r FROM Reservatie r " +
            "LEFT JOIN FETCH r.lokalen rl " +
            "LEFT JOIN FETCH rl.lokaal l " +
            "LEFT JOIN FETCH l.campus")
    List<Reservatie> findAllWithLokalen();


}

