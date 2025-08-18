package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodgingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LodgingService {
    private final LodgingRepository lodgingRepository;

    public LodgingService(LodgingRepository lodgingRepository) {
        this.lodgingRepository = lodgingRepository;
    }

    public List<Lodging> getAllLodgings() {
        return lodgingRepository.findAll();
    }
}

