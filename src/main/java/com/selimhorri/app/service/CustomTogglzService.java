package com.selimhorri.app.service;

import com.selimhorri.app.dto.TogglzFeatureDto;
import com.selimhorri.app.dto.response.collection.TogglzDtoResponse;

import java.util.List;

public interface CustomTogglzService {

    List<TogglzDtoResponse> findAll();
    TogglzDtoResponse toggleFeature(String featureName, TogglzFeatureDto dto);

}
