package com.crimeaware.crimeawareness.service;

import com.crimeaware.crimeawareness.dto.*;
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

    private static final List<String> CRIME_KEYWORDS = List.of(
            "murder", "theft", "robbery", "assault",
            "rape", "fraud", "kidnapping", "crime"
    );

    private static final List<String> VIDEO_KEYWORDS = List.of(
            "crime", "robbery", "arrest", "police", "cctv", "incident", "news"
    );

    private static final List<String> EXCLUDED_VIDEO_KEYWORDS = List.of(
            "song", "lyrics", "music", "movie", "trailer", "film", "official video"
    );

    public SearchResponseDTO searchCrime(String query, String location) {

        if (query == null || query.isBlank()) {
            return new SearchResponseDTO(List.of());
        }

        String cleanQuery = query.trim();
        String cleanLocation = (location == null || location.isBlank())
                ? null
                : location.trim();

        /* ================= NEWS API SEARCH ================= */

        List<NewsArticleDTO> newsResults;

        if (cleanLocation != null) {
            // Try city-specific search first
            newsResults = fetchNews(cleanQuery + " " + cleanLocation);

            // If no city results → fallback to global
            if (newsResults.isEmpty()) {
                newsResults = fetchNews(cleanQuery);
            }
        } else {
            // No location → global search
            newsResults = fetchNews(cleanQuery);
        }

        /* ================= YOUTUBE SEARCH ================= */

        List<VideoDTO> videos = fetchVideos(cleanQuery);

        /* ================= INCIDENT MAPPING ================= */

        List<IncidentDTO> incidents = newsResults.stream()
                .map(article -> new IncidentDTO(article, videos))
                .collect(Collectors.toList());

        return new SearchResponseDTO(incidents);
    }

    /* ================= FETCH NEWS ================= */

    private List<NewsArticleDTO> fetchNews(String searchTerm) {

        try {
            String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);

            String url = "https://newsapi.org/v2/everything"
                    + "?q=" + encoded
                    + "&searchIn=title,description"
                    + "&language=en"
                    + "&sortBy=relevancy"
                    + "&pageSize=20"
                    + "&apiKey=" + newsApiKey;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("articles")) {
                return List.of();
            }

            List<Map<String, Object>> articles =
                    (List<Map<String, Object>>) response.get("articles");

            return articles.stream()
                    .filter(Objects::nonNull)
                    .filter(this::isCrimeArticle)
                    .map(this::mapToNewsDTO)
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    /* ================= FETCH VIDEOS ================= */

    private List<VideoDTO> fetchVideos(String query) {

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&q=" + encoded
                    + "&type=video"
                    + "&maxResults=10"
                    + "&key=" + youtubeApiKey;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("items")) {
                return List.of();
            }

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) response.get("items");

            return items.stream()
                    .filter(this::isValidCrimeVideo)
                    .map(this::mapToVideoDTO)
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    /* ================= VALIDATION HELPERS ================= */

    private boolean isCrimeArticle(Map<String, Object> article) {
        String title = Objects.toString(article.get("title"), "").toLowerCase();
        String description = Objects.toString(article.get("description"), "").toLowerCase();
        String text = title + " " + description;

        return CRIME_KEYWORDS.stream().anyMatch(text::contains);
    }

    private boolean isValidCrimeVideo(Map<String, Object> video) {
        Map<String, Object> snippet =
                (Map<String, Object>) video.get("snippet");

        if (snippet == null) return false;

        String text =
                (Objects.toString(snippet.get("title"), "") + " " +
                        Objects.toString(snippet.get("description"), ""))
                        .toLowerCase();

        return VIDEO_KEYWORDS.stream().anyMatch(text::contains)
                && EXCLUDED_VIDEO_KEYWORDS.stream().noneMatch(text::contains);
    }

    /* ================= DTO MAPPERS ================= */

    private NewsArticleDTO mapToNewsDTO(Map<String, Object> article) {

        Map<String, String> source =
                (Map<String, String>) article.getOrDefault("source", Map.of());

        return new NewsArticleDTO(
                Objects.toString(article.get("title"), ""),
                Objects.toString(article.get("description"), ""),
                Objects.toString(article.get("url"), ""),
                Objects.toString(article.get("urlToImage"), ""),
                source.getOrDefault("name", "Unknown")
        );
    }

    private VideoDTO mapToVideoDTO(Map<String, Object> video) {

        Map<String, Object> snippet =
                (Map<String, Object>) video.get("snippet");

        Map<String, Object> id =
                (Map<String, Object>) video.get("id");

        if (snippet == null || id == null) {
            return new VideoDTO("", "", "");
        }

        String videoId = Objects.toString(id.get("videoId"), "");

        Map<String, Object> thumbnails =
                (Map<String, Object>) snippet.getOrDefault("thumbnails", Map.of());

        Map<String, Object> high =
                (Map<String, Object>) thumbnails.getOrDefault("high", Map.of());

        return new VideoDTO(
                Objects.toString(snippet.get("title"), ""),
                "https://www.youtube.com/watch?v=" + videoId,
                Objects.toString(high.get("url"), "")
        );
    }
}
