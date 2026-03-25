package com.crimeaware.crimeawareness.controller;

import com.crimeaware.crimeawareness.dto.SearchResponseDTO;
import com.crimeaware.crimeawareness.service.CrimeSearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crime")
@CrossOrigin(origins = "*")
public class CrimeController {

    private final CrimeSearchService crimeSearchService;

    public CrimeController(CrimeSearchService crimeSearchService) {
        this.crimeSearchService = crimeSearchService;
    }

    @GetMapping("/search")
    public SearchResponseDTO searchCrime(
            @RequestParam String query,
            @RequestParam(required = false) String location
    ) {

        return crimeSearchService.searchCrime(query, location);
    }
}