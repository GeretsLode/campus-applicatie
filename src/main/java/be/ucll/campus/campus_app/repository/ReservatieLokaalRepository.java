package be.ucll.campus.campus_app.repository;

import be.ucll.campus.campus_app.model.Lokaal;
import be.ucll.campus.campus_app.model.Reservatie;
import be.ucll.campus.campus_app.model.ReservatieLokaal;
import be.ucll.campus.campus_app.model.ReservatieLokaalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ReservatieLokaalRepository extends JpaRepository<ReservatieLokaal, ReservatieLokaalId> {
    void deleteById(ReservatieLokaalId id);
    boolean existsByReservatieAndLokaal(Reservatie reservatie, Lokaal lokaal);
    List<ReservatieLokaal> findByReservatie(Reservatie reservatie);
    List<ReservatieLokaal> findByLokaal(Lokaal lokaal);
}


