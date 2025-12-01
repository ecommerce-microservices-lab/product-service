package com.selimhorri.app.resource;

import com.selimhorri.app.dto.TogglzFeatureDto;
import com.selimhorri.app.dto.response.collection.TogglzDtoResponse;
import com.selimhorri.app.service.CustomTogglzService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actuator/togglz")
@Slf4j
@RequiredArgsConstructor
public class CustomTogglzResource {

    private final CustomTogglzService customTogglzService;

    @GetMapping
    public ResponseEntity<List<TogglzDtoResponse>> findAll() {
        log.info("Fetching all Togglz feature states");
        return ResponseEntity.ok(this.customTogglzService.findAll());

    }

    @PostMapping("/{featureName}")
    public ResponseEntity<TogglzDtoResponse> toggleFeature(@PathVariable String featureName, @RequestBody TogglzFeatureDto dto) {
        log.info("Toggling feature: {}", featureName);
        return ResponseEntity.ok(this.customTogglzService.toggleFeature(featureName, dto));
    }

}
