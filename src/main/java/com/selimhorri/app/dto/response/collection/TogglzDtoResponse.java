package com.selimhorri.app.dto.response.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TogglzDtoResponse {
    private String name;
    private boolean enabled;
    private String strategy;
    private Map<String, String> params;
}
