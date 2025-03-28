package be.ucll.campus.campus_app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Campus {

    @Id
    private String naam; // Primary Key

    @Column(nullable = false)
    private String adres;

    @Column(nullable = false)
    private int parkeerplaatsen;

    @OneToMany(mappedBy = "campus", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Lokaal> lokalen = new ArrayList<>();

    @Transient
    public int getAantalLokalen() {
        return lokalen.size(); // Dynamisch berekend
    }

    // Constructors, Getters en Setters

    // **Lege constructor vereist door Hibernate**
    public Campus() {}

    public Campus(String naam, String adres, int parkeerplaatsen) {
        this.naam = naam;
        this.adres = adres;
        this.parkeerplaatsen = parkeerplaatsen;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public int getParkeerplaatsen() {
        return parkeerplaatsen;
    }

    public void setParkeerplaatsen(int parkeerplaatsen) {
        this.parkeerplaatsen = parkeerplaatsen;
    }

    public String getNaam() {
        return naam;
    }


}
