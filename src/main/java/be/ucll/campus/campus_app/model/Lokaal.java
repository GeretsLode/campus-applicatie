package be.ucll.campus.campus_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "lokaal")
public class Lokaal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Naam mag niet leeg zijn")
    @Column(name = "naam", nullable = false, unique = true)
    private String naam;

    @NotBlank(message = "Type mag niet leeg zijn")
    @Column(name = "type", nullable = false)
    private String type;

    @Min(value = 1, message = "Capaciteit moet minstens 1 zijn")
    @Column(name = "capaciteit", nullable = false)
    private int capaciteit;

    @Column(name = "verdieping", nullable = false)
    private int verdieping;

    @ManyToOne(optional = false)
    @JsonIgnore  // Voorkomt oneindige JSON-lus
    @JoinColumn(name = "campus_naam", referencedColumnName = "naam", nullable = false)
    private Campus campus;

    public Lokaal() {
        // Lege constructor nodig voor JPA
    }

    public Lokaal(String naam, String type, int capaciteit, int verdieping, String campusNaam) {
        this.naam = naam;
        this.type = type;
        this.capaciteit = capaciteit;
        this.verdieping = verdieping;
        this.campus = campus;
    }

    // **Getters en Setters**
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNaam() { return naam; }
    public void setNaam(String naam) { this.naam = naam; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapaciteit() { return capaciteit; }
    public void setCapaciteit(int capaciteit) { this.capaciteit = capaciteit; }

    public int getVerdieping() { return verdieping; }
    public void setVerdieping(int verdieping) { this.verdieping = verdieping; }

    public Campus getCampus() { return campus; }
    public void setCampus(Campus campus) { this.campus = campus; }
}
