package com.crimeaware.crimeawareness.dto;

import java.util.List;

public class IncidentDTO {

    private NewsArticleDTO article;
    private List<VideoDTO> videos;

    public IncidentDTO(NewsArticleDTO article, List<VideoDTO> videos) {
        this.article = article;
        this.videos = videos;
    }

    public NewsArticleDTO getArticle() {
        return article;
    }

    public List<VideoDTO> getVideos() {
        return videos;
    }
}
