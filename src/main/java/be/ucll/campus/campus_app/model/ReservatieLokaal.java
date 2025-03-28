package be.ucll.campus.campus_app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservatie_lokaal")
public class ReservatieLokaal {

    @EmbeddedId
    private ReservatieLokaalId id;

    @ManyToOne
    @MapsId("reservatieId")
    @JoinColumn(name = "reservatie_id", nullable = false)
    private Reservatie reservatie;

    @ManyToOne
    @MapsId("lokaalId")
    @JoinColumn(name = "lokaal_id", nullable = false)
    private Lokaal lokaal;

    public ReservatieLokaal() {}

    public ReservatieLokaal(Reservatie reservatie, Lokaal lokaal) {
        this.id = new ReservatieLokaalId(reservatie.getId(), lokaal.getId());
        this.reservatie = reservatie;
        this.lokaal = lokaal;
    }

    public ReservatieLokaalId getId() {
        return id;
    }

    public Reservatie getReservatie() {
        return reservatie;
    }

    public Lokaal getLokaal() {
        return lokaal;
    }
    public void setId(ReservatieLokaalId id) {
        this.id = id;
    }

    public void setReservatie(Reservatie reservatie) {
        this.reservatie = reservatie;
    }

    public void setLokaal(Lokaal lokaal) {
        this.lokaal = lokaal;
    }
}


