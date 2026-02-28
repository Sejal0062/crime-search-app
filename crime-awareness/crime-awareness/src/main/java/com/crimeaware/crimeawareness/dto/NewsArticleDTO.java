package com.crimeaware.crimeawareness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsArticleDTO {
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String source;
}