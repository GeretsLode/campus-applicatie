package be.ucll.campus.campus_app.dto;

import be.ucll.campus.campus_app.model.User;

import java.time.LocalDate;

public class KlantDTO {
    private Long id;
    private String naam;
    private String voornaam;
    private String email;
    private LocalDate geboortedatum;

    public KlantDTO() {}

    public KlantDTO(Long id, String naam, String voornaam, String email, LocalDate geboortedatum) {
        this.id = id;
        this.naam = naam;
        this.voornaam = voornaam;
        this.email = email;
        this.geboortedatum = geboortedatum;
    }

    public static KlantDTO fromUser(User gebruiker) {
        return new KlantDTO(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getVoornaam(),
                gebruiker.getEmail(),
                gebruiker.getGeboortedatum()
        );
    }

    // Getters en setters
    public Long getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public void setVoornaam(String voornaam) {
        this.voornaam = voornaam;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }
}
