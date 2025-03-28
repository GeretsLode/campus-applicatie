package be.ucll.campus.campus_app.dto;

import be.ucll.campus.campus_app.model.Reservatie;
import java.time.LocalDateTime;
import java.util.List;

public class ReservatieDTO {
    private Long id;
    private LocalDateTime startTijd;
    private LocalDateTime eindTijd;
    private String commentaar;
    private KlantDTO gebruiker;
    private String campusNaam;
    private List<LokaalDTO> lokalen;

    // Constructor
    public ReservatieDTO(Long id, LocalDateTime startTijd, LocalDateTime eindTijd, String commentaar, KlantDTO gebruiker, String campusNaam, List<LokaalDTO> lokalen ) {
        this.id = id;
        this.startTijd = startTijd;
        this.eindTijd = eindTijd;
        this.commentaar = commentaar;
        this.gebruiker = gebruiker;
        this.campusNaam = campusNaam;
        this.lokalen = lokalen;
    }

    // Conversiemethode van entity naar DTO
    public static ReservatieDTO fromReservatie(Reservatie reservatie) {
        String campusNaam = reservatie.getLokalen().stream()
                .findFirst()
                .map(rl -> rl.getLokaal().getCampus().getNaam())
                .orElse(null);
        List<LokaalDTO> lokaalDTOs = reservatie.getLokalen().stream()
                .map(rl -> LokaalDTO.fromLokaal(rl.getLokaal()))
                .toList();
        return new ReservatieDTO(
                reservatie.getId(),
                reservatie.getStartTijd(),
                reservatie.getEindTijd(),
                reservatie.getCommentaar(),
                KlantDTO.fromUser(reservatie.getGebruiker()),
                campusNaam,
                lokaalDTOs
        );
    }

    // Getters en setters (eventueel gegenereerd via IDE)
    public Long getId() { return id; }
    public String getCampusNaam() {
        return campusNaam;
    }
    public LocalDateTime getStartTijd() { return startTijd; }
    public LocalDateTime getEindTijd() { return eindTijd; }
    public String getCommentaar() { return commentaar; }
    public KlantDTO getGebruiker() { return gebruiker; }
    public List<LokaalDTO> getLokalen() { return lokalen; }
}
