package bitc.full502.spring.service;

import bitc.full502.spring.domain.repository.LodgingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocationService {
    private final LodgingRepository lodgingRepository;

    public LocationService(LodgingRepository lodgingRepository) {
        this.lodgingRepository = lodgingRepository;
    }

    public List<String> getCities() {
        return lodgingRepository.findDistinctCities();
    }

    public List<String> getTowns(String city) {
        return lodgingRepository.findDistinctTownsByCity(city);
    }

    public List<String> getVills(String city, String town) {
        return lodgingRepository.findDistinctVills(city, town);
    }
}