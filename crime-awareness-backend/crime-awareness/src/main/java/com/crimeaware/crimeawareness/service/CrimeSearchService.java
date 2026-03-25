package com.crimeaware.crimeawareness.service;

import com.crimeaware.crimeawareness.dto.*;
import com.crimeaware.crimeawareness.entity.News;
import com.crimeaware.crimeawareness.repository.NewsRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrimeSearchService {

    @Value("${news.api.key}")
    private String newsApiKey;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final NewsRepository newsRepository;

    public CrimeSearchService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public SearchResponseDTO searchCrime(String query, String location) {

        if (query == null || query.isBlank()) {
            return new SearchResponseDTO(List.of());
        }

        String cleanQuery;

        if (location != null && !location.isBlank()) {
            cleanQuery = query + " " + location;
        } else {
            cleanQuery = query;
        }

        List<NewsArticleDTO> newsResults = fetchNews(cleanQuery);

        List<VideoDTO> videos = fetchVideos(cleanQuery);

        List<IncidentDTO> incidents = newsResults.stream()
                .map(article -> new IncidentDTO(article, videos))
                .collect(Collectors.toList());

        return new SearchResponseDTO(incidents);
    }

    /* ================= NEWS API ================= */

    private List<NewsArticleDTO> fetchNews(String query) {

        try {

            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String newsUrl =
                    "https://newsapi.org/v2/everything?q="
                            + encoded
                            + "&language=en"
                            + "&pageSize=10"
                            + "&apiKey=" + newsApiKey;

            System.out.println("NEWS API CALL: " + newsUrl);

            Map response = restTemplate.getForObject(newsUrl, Map.class);

            if (response == null || !response.containsKey("articles")) {
                return List.of();
            }

            List<Map<String, Object>> articles =
                    (List<Map<String, Object>>) response.get("articles");

            return articles.stream()
                    .map(this::mapToNewsDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            System.out.println("News API Error: " + e.getMessage());
            return List.of();
        }
    }

    /* ================= YOUTUBE API ================= */

    private List<VideoDTO> fetchVideos(String query) {

        try {

            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url =
                    "https://www.googleapis.com/youtube/v3/search"
                            + "?part=snippet"
                            + "&q=" + encoded
                            + "&type=video"
                            + "&maxResults=5"
                            + "&key=" + youtubeApiKey;

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("items")) {
                return List.of();
            }

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) response.get("items");

            return items.stream()
                    .map(this::mapToVideoDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {

            return List.of();
        }
    }

    /* ================= DTO MAPPING ================= */

    private NewsArticleDTO mapToNewsDTO(Map article) {

        Map source = (Map) article.get("source");

        return new NewsArticleDTO(
                Objects.toString(article.get("title"), ""),
                Objects.toString(article.get("description"), ""),
                Objects.toString(article.get("url"), ""),
                Objects.toString(article.get("urlToImage"), ""),
                source != null ? Objects.toString(source.get("name"), "") : ""
        );
    }

    private VideoDTO mapToVideoDTO(Map video) {

        Map snippet = (Map) video.get("snippet");
        Map id = (Map) video.get("id");

        String videoId = id != null ? Objects.toString(id.get("videoId"), "") : "";

        Map thumbnails = snippet != null ? (Map) snippet.get("thumbnails") : null;
        Map medium = thumbnails != null ? (Map) thumbnails.get("medium") : null;

        String thumbnail = medium != null ? Objects.toString(medium.get("url"), "") : "";

        return new VideoDTO(
                snippet != null ? Objects.toString(snippet.get("title"), "") : "",
                "https://www.youtube.com/watch?v=" + videoId,
                thumbnail
        );
    }
}