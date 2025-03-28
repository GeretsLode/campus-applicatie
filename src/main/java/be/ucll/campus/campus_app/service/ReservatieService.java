package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.dto.LokaalDTO;
import be.ucll.campus.campus_app.model.*;
import be.ucll.campus.campus_app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import be.ucll.campus.campus_app.exception.*;

@Service
public class ReservatieService {

    private final ReservatieRepository reservatieRepository;
    private final UserRepository userRepository;
    private final LokaalRepository lokaalRepository;
    private final ReservatieLokaalRepository reservatieLokaalRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    public ReservatieService(ReservatieRepository reservatieRepository, UserRepository userRepository,
                             LokaalRepository lokaalRepository, ReservatieLokaalRepository reservatieLokaalRepository) {
        this.reservatieRepository = reservatieRepository;
        this.userRepository = userRepository;
        this.lokaalRepository = lokaalRepository;
        this.reservatieLokaalRepository = reservatieLokaalRepository;
    }

    public List<Reservatie> getAllReservaties() {
        return reservatieRepository.findAllWithLokalen();
    }

    public Optional<Reservatie> getReservatieById(Long id) {
        return reservatieRepository.findById(id);
    }

    public List<Reservatie> getReservatiesByUserId(Long userId) {
        return reservatieRepository.findByGebruikerId(userId);
    }

    @Transactional
    public Reservatie addReservatie(Long userId, String startTijdInput, String eindTijdInput, Set<Long> lokaalIds) {
        User gebruiker = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidReservationException("Gebruiker niet gevonden"));

        LocalDateTime startTijd;
        LocalDateTime eindTijd;

        try {
            startTijd = LocalDateTime.parse(startTijdInput.trim(), FORMATTER);
            eindTijd = LocalDateTime.parse(eindTijdInput.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                DateTimeFormatter fallback = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                startTijd = LocalDateTime.parse(startTijdInput.trim(), fallback);
                eindTijd = LocalDateTime.parse(eindTijdInput.trim(), fallback);
            } catch (DateTimeParseException e2) {
                throw new InvalidReservationException("Ongeldig datumformaat. Gebruik: yyyy-MM-dd'T'HH:mm of yyyy-MM-dd'T'HH:mm:ss");
            }
        }

        // 1. Controle: Starttijd moet vóór de eindtijd liggen
        if (startTijd.isAfter(eindTijd)) {
            throw new InvalidReservationException("Starttijd moet vóór de eindtijd liggen.");
        }

        // 2. Controle: Reservatie mag niet in het verleden liggen
        if (eindTijd.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("Reservatie kan niet in het verleden liggen.");
        }

        // 3. Controle: Overlappende reservaties vermijden
        for (Long lokaalId : lokaalIds) {
            if (reservatieRepository.existsByLokaalIdAndOverlappingTijd(lokaalId, startTijd, eindTijd)) {
                throw new InvalidReservationException("Lokaal is al gereserveerd in deze periode.");
            }
        }

        Reservatie reservatie = new Reservatie();
        reservatie.setGebruiker(gebruiker);
        reservatie.setStartTijd(startTijd);
        reservatie.setEindTijd(eindTijd);
        Reservatie nieuweReservatie = reservatieRepository.save(reservatie);

        for (Long lokaalId : lokaalIds) {
            Lokaal lokaal = lokaalRepository.findById(lokaalId)
                    .orElseThrow(() -> new InvalidReservationException("Lokaal niet gevonden"));

            ReservatieLokaal reservatieLokaal = new ReservatieLokaal();
            ReservatieLokaalId reservatieLokaalId = new ReservatieLokaalId(nieuweReservatie.getId(), lokaal.getId());

            reservatieLokaal.setId(reservatieLokaalId);
            reservatieLokaal.setReservatie(nieuweReservatie);
            reservatieLokaal.setLokaal(lokaal);


            reservatieLokaalRepository.saveAndFlush(reservatieLokaal);
        }

        return nieuweReservatie;
    }

    @Transactional
    public void deleteReservatie(Long id) {
        if (!reservatieRepository.existsById(id)) {
            throw new InvalidReservationException("Reservatie niet gevonden.");
        }
        Reservatie reservatie = reservatieRepository.findById(id)
                .orElseThrow(() -> new InvalidReservationException("Reservatie niet gevonden"));
        List<ReservatieLokaal> reservatieLokalen = reservatieLokaalRepository.findByReservatie(reservatie);

        for (ReservatieLokaal reservatieLokaal : reservatieLokalen) {
            reservatieLokaalRepository.deleteById(reservatieLokaal.getId());
        }

        reservatieRepository.deleteById(id);
    }
    public List<LokaalDTO> findBeschikbareLokalen(String campusNaam, String startStr, String eindStr, Integer minPersonen) {
        LocalDateTime start, eind;
        try {
            start = LocalDateTime.parse(startStr.trim(), FORMATTER);
            eind = LocalDateTime.parse(eindStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidReservationException("Ongeldig datumformaat bij ophalen beschikbare lokalen.");
        }


        return lokaalRepository.findByCampusNaam(campusNaam).stream()
                .filter(l -> minPersonen == null || l.getCapaciteit() >= minPersonen)
                .filter(l -> !reservatieRepository.existsByLokaalIdAndOverlappingTijd(l.getId(), start, eind))
                .map(LokaalDTO::fromLokaal)
                .toList();
    }

    public List<LokaalDTO> findBeschikbareLokalen(String campusNaam, String startStr, String eindStr, Integer minPersonen, Long reservatieId) {
        LocalDateTime start, eind;
        try {
            start = LocalDateTime.parse(startStr.trim(), FORMATTER);
            eind = LocalDateTime.parse(eindStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidReservationException("Ongeldig datumformaat bij ophalen beschikbare lokalen.");
        }

        Reservatie reservatie = reservatieRepository.findById(reservatieId)
                .orElseThrow(() -> new InvalidReservationException("Reservatie niet gevonden"));

        Set<Long> lokaalIdsInReservatie = reservatie.getLokalen().stream()
                .map(rl -> rl.getLokaal().getId())
                .collect(java.util.stream.Collectors.toSet());

        return lokaalRepository.findByCampusNaam(campusNaam).stream()
                .filter(l -> minPersonen == null || l.getCapaciteit() >= minPersonen)
                .filter(l -> !reservatieRepository.existsOverlappingExceptOwn(l.getId(), start, eind, reservatieId))
                .filter(l -> !lokaalIdsInReservatie.contains(l.getId()))
                .map(LokaalDTO::fromLokaal)
                .toList();
    }


    @Transactional
    public Optional<Reservatie> voegLokaalToeAanReservatie(Long userId, Long reservatieId, Long lokaalId) {
        Optional<Reservatie> optionalReservatie = reservatieRepository.findById(reservatieId);
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Lokaal> optionalLokaal = lokaalRepository.findById(lokaalId);

        if (optionalReservatie.isEmpty() || optionalUser.isEmpty() || optionalLokaal.isEmpty()) {
            return Optional.empty();
        }

        Reservatie reservatie = optionalReservatie.get();
        User gebruiker = optionalUser.get();
        Lokaal lokaal = optionalLokaal.get();

        if (!reservatie.getGebruiker().getId().equals(userId)) {
            throw new InvalidReservationException("Reservatie behoort niet tot deze gebruiker.");
        }

        if (reservatieRepository.existsByLokaalIdAndOverlappingTijd(lokaalId, reservatie.getStartTijd(), reservatie.getEindTijd())) {
            throw new InvalidReservationException("Dit lokaal is al gereserveerd in deze periode.");
        }

        ReservatieLokaalId compositeId = new ReservatieLokaalId(reservatie.getId(), lokaal.getId());
        if (reservatieLokaalRepository.existsById(compositeId)) {
            throw new InvalidReservationException("Lokaal is al gekoppeld aan deze reservatie.");
        }

        ReservatieLokaal nieuw = new ReservatieLokaal();
        nieuw.setId(compositeId);
        nieuw.setReservatie(reservatie);
        nieuw.setLokaal(lokaal);
        reservatieLokaalRepository.save(nieuw);

        return Optional.of(reservatieRepository.findById(reservatieId).get());
    }

    public List<Reservatie> getReservatiesVoorLokaal(String campusNaam, Long lokaalId) {
        Optional<Lokaal> lokaalOpt = lokaalRepository.findById(lokaalId);
        if (lokaalOpt.isEmpty()) {
            throw new InvalidReservationException("Lokaal niet gevonden.");
        }

        Lokaal lokaal = lokaalOpt.get();
        if (!lokaal.getCampus().getNaam().equalsIgnoreCase(campusNaam)) {
            throw new InvalidReservationException("Lokaal behoort niet tot campus " + campusNaam);
        }

        return reservatieLokaalRepository.findByLokaal(lokaal).stream()
                .map(ReservatieLokaal::getReservatie)
                .distinct()
                .toList();
    }

    public List<Reservatie> getReservatiesVoorCampus(String campusNaam) {
        return reservatieRepository.findAll().stream()
                .filter(r -> r.getLokalen().stream()
                        .anyMatch(rl -> rl.getLokaal().getCampus().getNaam().equalsIgnoreCase(campusNaam)))
                .distinct()
                .toList();
    }


}
