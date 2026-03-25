package com.crimeaware.crimeawareness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data

public class VideoDTO {

    private String title;
    private String url;
    private String thumbnail;

    public VideoDTO(String title, String url, String thumbnail) {
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
    }

    public String getTitle() { return title; }

    public String getUrl() { return url; }

    public String getThumbnail() { return thumbnail; }
}