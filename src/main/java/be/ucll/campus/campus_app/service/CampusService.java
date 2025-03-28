package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.model.Campus;
import be.ucll.campus.campus_app.repository.CampusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CampusService {
    private final CampusRepository campusRepository;

    public CampusService(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    public Campus addCampus(Campus campus) {
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
