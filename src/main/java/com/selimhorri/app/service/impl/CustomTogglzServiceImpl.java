package com.selimhorri.app.service.impl;

import com.selimhorri.app.dto.TogglzFeatureDto;
import com.selimhorri.app.dto.response.collection.TogglzDtoResponse;
import com.selimhorri.app.service.CustomTogglzService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CustomTogglzServiceImpl implements CustomTogglzService {

    private final FeatureManager featureManager;

    @Override
    public List<TogglzDtoResponse> findAll() {
        return featureManager
                .getFeatures()
                .stream()
                .map(f -> {
                    FeatureState state = featureManager.getFeatureState(f);
                    return TogglzDtoResponse.builder()
                            .name(f.name())
                            .enabled(state.isEnabled())
                            .strategy(state.getStrategyId())
                            .params(state.getParameterMap())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public TogglzDtoResponse toggleFeature(String featureName, TogglzFeatureDto dto) {
        Feature feature = new NamedFeature(featureName);
        FeatureState state = featureManager.getFeatureState(feature);

        state.setEnabled(dto.isEnabled());

        featureManager.setFeatureState(state);

        return TogglzDtoResponse.builder()
                .name(featureName)
                .enabled(state.isEnabled())
                .strategy(state.getStrategyId())
                .params(state.getParameterMap())
                .build();
    }
}
