package com.crimeaware.crimeawareness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponseDTO {

    private List<IncidentDTO> incidents;

}