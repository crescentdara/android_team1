package bitc.full502.spring.controller;

import bitc.full502.spring.service.LocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 위치 선택 API
 * - GET /api/locations/cities
 * - GET /api/locations/towns?city=제주시
 * - GET /api/locations/vills?city=제주시&town=아라동
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/cities")
    public List<String> cities() {
        return locationService.getCities();
    }

    @GetMapping("/towns")
    public List<String> towns(@RequestParam String city) {
        return locationService.getTowns(city);
    }

    @GetMapping("/vills")
    public List<String> vills(@RequestParam String city, @RequestParam String town) {
        return locationService.getVills(city, town);
    }
}