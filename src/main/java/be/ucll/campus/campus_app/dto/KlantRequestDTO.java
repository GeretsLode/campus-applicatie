package be.ucll.campus.campus_app.dto;

import java.time.LocalDate;

public class KlantRequestDTO {
    private String naam;
    private String voornaam;
    private String email;
    private LocalDate geboortedatum;

    public KlantRequestDTO() {
    }

    public KlantRequestDTO(String naam, String voornaam, String email, LocalDate geboortedatum) {
        this.naam = naam;
        this.voornaam = voornaam;
        this.email = email;
        this.geboortedatum = geboortedatum;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }
}
