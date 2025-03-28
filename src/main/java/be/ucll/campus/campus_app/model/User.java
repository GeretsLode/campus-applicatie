package be.ucll.campus.campus_app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Email moet een geldig formaat hebben")
    @NotBlank(message = "Email mag niet leeg zijn")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Naam mag niet leeg zijn")
    @Size(min = 2, message = "Naam moet minstens 2 karakters bevatten")
    private String naam;

    @NotBlank(message = "Voornaam mag niet leeg zijn")
    @Size(min = 2, message = "Voornaam moet minstens 2 karakters bevatten")
    private String voornaam;

    @Past(message = "Geboortedatum moet in het verleden liggen")
    @NotNull(message = "Geboortedatum mag niet leeg zijn")
    private LocalDate geboortedatum;

    @OneToMany(mappedBy = "gebruiker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Reservatie> reservaties;

    public User() {
        // Hibernate vereist een lege constructor
    }

    public User(String naam, String voornaam, String email, LocalDate geboortedatum) {
        this.naam = naam;
        this.voornaam = voornaam;
        this.email = email;
        this.geboortedatum = geboortedatum;
    }

    // Getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public void setVoornaam(String voornaam) {
        this.voornaam = voornaam;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    public List<Reservatie> getReservaties() {
        return reservaties;
    }

    public void setReservaties(List<Reservatie> reservaties) {
        this.reservaties = reservaties;
    }
}
