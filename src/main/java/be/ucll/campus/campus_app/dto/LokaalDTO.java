package be.ucll.campus.campus_app.dto;

import be.ucll.campus.campus_app.model.Lokaal;

public class LokaalDTO {
    private Long id;
    private String naam;
    private int capaciteit;

    public LokaalDTO(Long id, String naam, int capaciteit) {
        this.id = id;
        this.naam = naam;
        this.capaciteit = capaciteit;
    }

    public static LokaalDTO fromLokaal(Lokaal lokaal) {
        return new LokaalDTO(
                lokaal.getId(),
                lokaal.getNaam(),
                lokaal.getCapaciteit()
        );
    }

    public Long getId() { return id; }
    public String getNaam() { return naam; }
    public int getCapaciteit() { return capaciteit; }
}
