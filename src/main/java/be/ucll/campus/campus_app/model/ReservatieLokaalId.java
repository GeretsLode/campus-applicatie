package be.ucll.campus.campus_app.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReservatieLokaalId implements Serializable {

    private Long reservatieId;
    private Long lokaalId;

    public ReservatieLokaalId() {}

    public ReservatieLokaalId(Long reservatieId, Long lokaalId) {
        this.reservatieId = reservatieId;
        this.lokaalId = lokaalId;
    }

    public Long getReservatieId() {
        return reservatieId;
    }

    public void setReservatieId(Long reservatieId) {
        this.reservatieId = reservatieId;
    }

    public Long getLokaalId() {
        return lokaalId;
    }

    public void setLokaalId(Long lokaalId) {
        this.lokaalId = lokaalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservatieLokaalId that = (ReservatieLokaalId) o;
        return reservatieId.equals(that.reservatieId) && lokaalId.equals(that.lokaalId);
    }

    @Override
    public int hashCode() {
        return reservatieId.hashCode() + lokaalId.hashCode();
    }
}
