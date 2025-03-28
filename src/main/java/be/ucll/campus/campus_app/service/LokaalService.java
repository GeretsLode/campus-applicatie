package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.exception.DuplicateResourceException;
import be.ucll.campus.campus_app.model.Lokaal;
import be.ucll.campus.campus_app.repository.LokaalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LokaalService {

    private final LokaalRepository lokaalRepository;

    @Autowired
    public LokaalService(LokaalRepository lokaalRepository) {
        this.lokaalRepository = lokaalRepository;
    }

    public List<Lokaal> getLokalenByCampus(String campusNaam) {
        return lokaalRepository.findByCampusNaam(campusNaam);
    }

    public Optional<Lokaal> getLokaalByIdAndCampus(Long id, String campusNaam) {
        return lokaalRepository.findByIdAndCampusNaam(id, campusNaam);
    }

    public Lokaal addLokaalToCampus(Lokaal lokaal) {
        if (lokaal.getCampus() == null) {
            throw new RuntimeException("Campus mag niet null zijn!");
        }
        boolean bestaatAl = lokaalRepository.existsByCampusNaamAndNaam(
                lokaal.getCampus().getNaam(),
                lokaal.getNaam()
        );

        if (bestaatAl) {
            throw new DuplicateResourceException("Er bestaat al een lokaal met deze naam binnen campus '"
                    + lokaal.getCampus().getNaam() + "'.");
        }

        return lokaalRepository.save(lokaal);
    }


    public void deleteLokaal(Long id) {
        lokaalRepository.deleteById(id);
    }
}
