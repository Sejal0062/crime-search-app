package com.crimeaware.crimeawareness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data

public class SearchResponseDTO {

    private List<IncidentDTO> incidents;

    public SearchResponseDTO(List<IncidentDTO> incidents) {
        this.incidents = incidents;
    }

    public List<IncidentDTO> getIncidents() {
        return incidents;
    }
}




