package com.crimeaware.crimeawareness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoDTO {
    private String title;
    private String videoUrl;
    private String thumbnail;
}
