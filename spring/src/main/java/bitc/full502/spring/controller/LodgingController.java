package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.service.LodgingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LodgingController {
    private final LodgingService lodgingService;

    public LodgingController(LodgingService lodgingService) {
        this.lodgingService = lodgingService;
    }

    @GetMapping("/lodgings")
    public List<Lodging> getAllLodgings() {
        return lodgingService.getAllLodgings();
    }
}
