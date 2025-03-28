package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.exception.DuplicateResourceException;
import be.ucll.campus.campus_app.exception.ResourceNotFoundException;
import be.ucll.campus.campus_app.model.Campus;
import be.ucll.campus.campus_app.repository.CampusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CampusService {
    private final CampusRepository campusRepository;

    @Autowired
    public CampusService(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    public Campus addCampus(Campus campus) {
        Optional<Campus> bestaande = campusRepository.findById(campus.getNaam());

        if (bestaande.isPresent()) {
            throw new DuplicateResourceException("Campus met naam '" + campus.getNaam() + "' bestaat al.");
        }

        return campusRepository.save(campus);
    }

    public List<Campus> getAllCampuses() {
        return campusRepository.findAll();
    }

    public Optional<Campus> getCampusById(String naam) {
        return campusRepository.findById(naam);
    }

    public Optional<Campus> getCampusByNaam(String campusNaam) {
        return campusRepository.findById(campusNaam);
    }


    public void deleteCampus(String naam) {
        Optional<Campus> campusOpt = campusRepository.findById(naam);
        if (campusOpt.isEmpty()) {
            throw new ResourceNotFoundException("Campus met naam '" + naam + "' niet gevonden.");
        }

        campusRepository.deleteById(naam);
    }

    public Optional<Campus> updateCampus(String naam, Campus updatedCampus) {
        return campusRepository.findById(naam).map(existingCampus -> {
            existingCampus.setAdres(updatedCampus.getAdres());
            existingCampus.setParkeerplaatsen(updatedCampus.getParkeerplaatsen());
            return campusRepository.save(existingCampus);
        });
    }

}
