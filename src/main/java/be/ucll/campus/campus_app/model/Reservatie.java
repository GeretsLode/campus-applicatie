package be.ucll.campus.campus_app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservatie")
public class Reservatie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FutureOrPresent(message = "Starttijd moet in de toekomst of heden liggen.")
    @NotNull(message = "Starttijd mag niet leeg zijn.")
    @Column(name = "start_tijd", nullable = false)
    private LocalDateTime startTijd;

    @FutureOrPresent(message = "Eindtijd moet in de toekomst of heden liggen.")
    @NotNull(message = "Eindtijd mag niet leeg zijn.")
    @Column(name = "eind_tijd", nullable = false)
    private LocalDateTime eindTijd;

    @Column(name = "commentaar")
    private String commentaar;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User gebruiker;

    @OneToMany(mappedBy = "reservatie", cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReservatieLokaal> lokalen = new HashSet<>();

    public Reservatie() {}

    public Reservatie(LocalDateTime startTijd, LocalDateTime eindTijd, String commentaar, User gebruiker) {
        this.startTijd = startTijd;
        this.eindTijd = eindTijd;
        this.commentaar = commentaar;
        this.gebruiker = gebruiker;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(LocalDateTime startTijd) {
        this.startTijd = startTijd;
    }

    public LocalDateTime getEindTijd() {
        return eindTijd;
    }

    public void setEindTijd(LocalDateTime eindTijd) {
        this.eindTijd = eindTijd;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public User getGebruiker() {
        return gebruiker;
    }

    public void setGebruiker(User gebruiker) {
        this.gebruiker = gebruiker;
    }

    public Set<ReservatieLokaal> getLokalen() {
        return lokalen;
    }

    public void setLokalen(Set<ReservatieLokaal> lokalen) {
        this.lokalen = lokalen;
    }
}
