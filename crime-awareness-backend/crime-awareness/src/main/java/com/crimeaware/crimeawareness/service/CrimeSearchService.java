package com.crimeaware.crimeawareness.service;

import com.crimeaware.crimeawareness.dto.*;
import com.crimeaware.crimeawareness.entity.News;
import com.crimeaware.crimeawareness.repository.NewsRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CrimeSearchService {

    @Value("${news.api.key:}")
    private String newsApiKey;

    @Value("${youtube.api.key:}")
    private String youtubeApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final NewsRepository newsRepository;

    private static final List<String> CRIME_WORDS = List.of(
            "crime", "murder", "murdered", "homicide",
            "killed", "killing", "robbery", "robbed",
            "theft", "stolen", "burglary", "fraud",
            "scam", "cybercrime", "cyber fraud",
            "phishing", "kidnapping", "kidnapped",
            "abduction", "assault", "rape", "extortion",
            "shooting", "trafficking", "arrested",
            "arrest", "police", "accused", "criminal",
            "smuggling", "seized", "looted", "snatching",
            "cheating", "defrauded", "swindled", "stealing",
            "thief", "thieves", "abducted", "cyber crime", "online scam"
    );

    private static final List<String> INDIA_WORDS = List.of(
            "india", "indian", "delhi", "mumbai",
            "pune", "maharashtra", "bengaluru",
            "bangalore", "hyderabad", "chennai",
            "kolkata", "jaipur", "lucknow", "patna",
            "nagpur", "nashik", "thane", "noida",
            "gurugram", "punjab", "haryana",
            "gujarat", "rajasthan", "uttar pradesh",
            "madhya pradesh", "bihar", "west bengal",
            "tamil nadu", "karnataka", "kerala",
            "telangana", "andhra pradesh", "odisha",
            "assam", "goa", "kashmir", "uttarakhand",
            "jharkhand", "chhattisgarh",
            "himachal pradesh", "manipur", "meghalaya", "mizoram",
            "nagaland", "tripura", "sikkim", "arunachal pradesh",
            "ladakh", "chandigarh", "puducherry", "andaman",
            "lakshadweep", "daman", "diu", "silvassa",
            "the hindu", "hindustan times",
            "times of india", "indian express",
            "ndtv", "india today", "news18"
    );

    private static final List<String> EXCLUDED_WORDS = List.of(
            "grand theft auto", "gta 6", "gta vi",
            "video game", "gaming", "gameplay",
            "movie review", "film review",
            "movie trailer", "official trailer",
            "box office", "music video",
            "song lyrics", "web series"
    );

    public CrimeSearchService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public SearchResponseDTO searchCrime(
            String query, String location) {

        String crime = query == null ? "" : query.trim();
        String city = location == null ? "" : location.trim();

        String cacheKey = "india-v3|"
                + crime.toLowerCase(Locale.ROOT)
                + "|"
                + city.toLowerCase(Locale.ROOT);

        List<NewsArticleDTO> articles = fetchNews(crime, city);

        if (!articles.isEmpty()) {
            for (NewsArticleDTO article : articles) {
                saveArticleIfNew(article, cacheKey);
            }
        } else {
            articles = loadStoredArticles(cacheKey);
        }

        List<VideoDTO> videos = articles.isEmpty()
                ? List.of()
                : fetchVideos(crime, city);

        List<IncidentDTO> incidents = new ArrayList<>();

        for (int i = 0; i < articles.size(); i++) {
            List<VideoDTO> relatedVideos =
                    i == 0 ? videos : List.of();

            incidents.add(
                    new IncidentDTO(articles.get(i), relatedVideos)
            );
        }

        return new SearchResponseDTO(incidents);
    }

    private List<NewsArticleDTO> fetchNews(
            String crime, String city) {

        if (newsApiKey == null || newsApiKey.isBlank()) {
            System.err.println("News API key is missing");
            return List.of();
        }

        String topic = crime.isBlank()
                ? "(murder OR robbery OR fraud OR kidnapping OR cybercrime OR assault OR theft OR arrested)"
                : crimeTerms(crime).stream()
                    .map(term -> "\"" + cleanSearch(term) + "\"")
                    .collect(Collectors.joining(" OR ", "(", ")"));

        String place = city.isBlank()
                ? "(India OR Indian OR Delhi OR Mumbai OR Pune OR Bengaluru OR Hyderabad OR Chennai OR Kolkata OR Maharashtra OR Gujarat OR Rajasthan OR Bihar OR Kerala OR Punjab OR Karnataka OR Assam OR Uttar Pradesh OR Madhya Pradesh OR Odisha OR Telangana OR Haryana OR Kashmir OR Goa OR Jharkhand OR Chhattisgarh OR Uttarakhand OR Tamil Nadu OR Andhra Pradesh OR West Bengal OR Himachal Pradesh OR Manipur OR Meghalaya OR Mizoram OR Nagaland OR Tripura OR Sikkim OR Arunachal Pradesh OR Ladakh OR Chandigarh OR Puducherry OR Andaman OR Lakshadweep OR Daman OR Diu OR Silvassa)"
                : "\"" + cleanSearch(city) + "\"";

        String searchQuery = topic + " AND " + place;
        // NewsAPI limits q to 500 characters; use local India filtering for long queries.
        if (searchQuery.length() > 500) {
            searchQuery = topic + " AND (India OR Indian OR Delhi OR Mumbai OR Pune OR Bengaluru OR Hyderabad OR Chennai OR Kolkata OR Maharashtra OR Gujarat OR Rajasthan OR Bihar OR Kerala OR Punjab OR Karnataka OR Assam OR Uttar Pradesh OR Madhya Pradesh OR Odisha OR Telangana OR Haryana OR Kashmir OR Goa OR Jharkhand OR Chhattisgarh OR Uttarakhand OR Tamil Nadu OR Andhra Pradesh OR West Bengal)";
            if (searchQuery.length() > 500) searchQuery = topic;
        }

        try {
            String url =
                    "https://newsapi.org/v2/everything?q="
                    + encode(searchQuery)
                    + "&searchIn=title,description"
                    + "&language=en"
                    + "&sortBy=publishedAt"
                    + "&pageSize=100"
                    + "&apiKey=" + encode(newsApiKey);

            Map<?, ?> response =
                    restTemplate.getForObject(URI.create(url), Map.class);

            if (response == null) {
                return List.of();
            }

            Object raw = response.get("articles");

            if (!(raw instanceof List<?>)) {
                return List.of();
            }

            List<?> rawArticles = (List<?>) raw;
            System.out.println("DEBUG: NewsAPI returned " + rawArticles.size() + " articles before filtering");

            Map<String, NewsArticleDTO> results =
                    new LinkedHashMap<>();

            Set<String> headlines = new HashSet<>();

            for (Object item : rawArticles) {

                if (!(item instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> article = (Map<?, ?>) item;

                String title = stringValue(article.get("title"));
                String description =
                        stringValue(article.get("description"));
                String articleUrl =
                        stringValue(article.get("url"));

                String sourceName = "Unknown";

                Object sourceObject = article.get("source");

                if (sourceObject instanceof Map<?, ?>) {
                    Map<?, ?> source = (Map<?, ?>) sourceObject;
                    sourceName = stringValue(source.get("name"));
                }

                if (!isRelevant(
                        title, description, sourceName,
                        crime, city)) {
                    continue;
                }

                if (!articleUrl.startsWith("http")) {
                    continue;
                }

                String headlineKey =
                        normalize(title).replaceAll(
                                "[^a-z0-9 ]", "").trim();

                if (!headlines.add(headlineKey)) {
                    continue;
                }

                NewsArticleDTO news = new NewsArticleDTO(
                        title,
                        description,
                        articleUrl,
                        stringValue(article.get("urlToImage")),
                        sourceName,
                        stringValue(article.get("publishedAt"))
                );

                results.putIfAbsent(articleUrl, news);

                if (results.size() >= 24) {
                    break;
                }
            }

            System.out.println("DEBUG: Articles remaining after filtering: " + results.size());
            return new ArrayList<>(results.values());

        } catch (Exception e) {
            System.err.println(
                    "News API error: "
                    + e.getClass().getSimpleName()
                    
            );

            return List.of();
        }
    }

    private boolean isRelevant(
            String title,
            String description,
            String source,
            String crime,
            String city) {

        String headline = normalize(title);
        String text = headline + " " + normalize(description);
        String publisher = normalize(source);

        if (headline.isBlank()
                || headline.equals("[removed]")) {
            return false;
        }

        for (String excluded : EXCLUDED_WORDS) {
            if (text.contains(excluded)) {
                return false;
            }
        }

        if (!containsAny(text, CRIME_WORDS)) {
            return false;
        }

        if (!containsAny(
                text + " " + publisher, INDIA_WORDS)) {
            return false;
        }

        if (!city.isBlank()
                && !containsWord(text, normalize(city))) {
            return false;
        }

        if (!crime.isBlank()) {

            List<String> alternatives = crimeTerms(crime);

            if (!containsAny(text, alternatives)) {
                return false;
            }
        }

        return true;
    }

    private List<String> crimeTerms(String crime) {
        String keyword = normalize(crime);

        List<String> alternatives = new ArrayList<>();
        alternatives.add(keyword);

        switch (keyword) {
            case "robbery":
                alternatives.addAll(List.of(
                        "robbed", "looted", "snatching",
                        "armed robbery"));
                break;

            case "theft":
                alternatives.addAll(List.of(
                        "stolen", "stealing", "burglary",
                        "thief", "thieves"));
                break;

            case "murder":
                alternatives.addAll(List.of(
                        "murdered", "homicide",
                        "killed", "killing"));
                break;

            case "fraud":
                alternatives.addAll(List.of(
                        "scam", "cheating",
                        "defrauded", "swindled"));
                break;

            case "cybercrime":
                alternatives.addAll(List.of(
                        "cyber crime", "cyber fraud",
                        "phishing", "online scam"));
                break;

            case "kidnapping":
                alternatives.addAll(List.of(
                        "kidnapped", "abduction",
                        "abducted"));
                break;

            default:
                break;
        }

        return alternatives;
    }

    private boolean containsAny(
            String text, List<String> words) {

        for (String word : words) {
            if (containsWord(text, word)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsWord(
            String text, String word) {

        if (word == null || word.isBlank()) {
            return false;
        }

        String regex = "(?<![\\p{L}\\p{N}])"
                + Pattern.quote(word)
                + "(?![\\p{L}\\p{N}])";

        return Pattern.compile(
                regex,
                Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE
        ).matcher(text).find();
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT).trim();
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String cleanSearch(String value) {
        return value.replace("\"", "")
                .replace("(", " ")
                .replace(")", " ")
                .trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void saveArticleIfNew(
            NewsArticleDTO article, String cacheKey) {

        try {
            if (newsRepository.existsByUrl(
                    article.getUrl())) {
                return;
            }

            News news = new News();

            news.setTitle(article.getTitle());
            news.setDescription(article.getDescription());
            news.setUrl(article.getUrl());
            news.setImageUrl(article.getImageUrl());
            news.setSource(article.getSource());
            news.setQuery(cacheKey);

            newsRepository.save(news);

        } catch (Exception e) {
            System.err.println(
                    "Could not cache article: "
                    + e.getMessage()
            );
        }
    }

    private List<NewsArticleDTO> loadStoredArticles(
            String cacheKey) {

        return newsRepository
                .findByQueryIgnoreCase(cacheKey)
                .stream()
                .map(news -> new NewsArticleDTO(
                        stringValue(news.getTitle()),
                        stringValue(news.getDescription()),
                        stringValue(news.getUrl()),
                        stringValue(news.getImageUrl()),
                        stringValue(news.getSource())
                ))
                .filter(article -> !article.getUrl().isBlank())
                .limit(24)
                .collect(Collectors.toList());
    }

    private List<VideoDTO> fetchVideos(
            String crime, String city) {

        if (youtubeApiKey == null
                || youtubeApiKey.isBlank()) {
            return List.of();
        }

        String searchText =
                (crime.isBlank() ? "crime" : crime)
                + " " + city + " India crime news";

        try {
            String url =
                    "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&q=" + encode(searchText)
                    + "&type=video"
                    + "&maxResults=10"
                    + "&key=" + encode(youtubeApiKey);

            Map<?, ?> response =
                    restTemplate.getForObject(URI.create(url), Map.class);

            if (response == null) {
                return List.of();
            }

            Object raw = response.get("items");

            if (!(raw instanceof List<?>)) {
                return List.of();
            }

            List<?> items = (List<?>) raw;

            Map<String, VideoDTO> videos =
                    new LinkedHashMap<>();

            for (Object item : items) {

                if (!(item instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> video = (Map<?, ?>) item;

                Map<?, ?> id = Map.of();
                Map<?, ?> snippet = Map.of();

                if (video.get("id") instanceof Map<?, ?>) {
                    id = (Map<?, ?>) video.get("id");
                }

                if (video.get("snippet") instanceof Map<?, ?>) {
                    snippet = (Map<?, ?>) video.get("snippet");
                }

                String videoId =
                        stringValue(id.get("videoId"));

                String title =
                        stringValue(snippet.get("title"));

                String description =
                        stringValue(snippet.get("description"));

                String text = normalize(
                        title + " " + description);

                if (videoId.isBlank()) {
                    continue;
                }

                if (!containsAny(text, CRIME_WORDS)) {
                    continue;
                }

                if (!containsAny(text, INDIA_WORDS)) {
                    continue;
                }

                if (!city.isBlank()
                        && !containsWord(
                                text, normalize(city))) {
                    continue;
                }

                boolean excluded = false;

                for (String word : EXCLUDED_WORDS) {
                    if (text.contains(word)) {
                        excluded = true;
                        break;
                    }
                }

                if (excluded) {
                    continue;
                }

                Map<?, ?> thumbnails = Map.of();

                if (snippet.get("thumbnails")
                        instanceof Map<?, ?>) {
                    thumbnails =
                            (Map<?, ?>) snippet.get("thumbnails");
                }

                Object thumbnail =
                        thumbnails.get("high");

                if (thumbnail == null) {
                    thumbnail = thumbnails.get("medium");
                }

                String imageUrl = "";

                if (thumbnail instanceof Map<?, ?>) {
                    Map<?, ?> image =
                            (Map<?, ?>) thumbnail;

                    imageUrl =
                            stringValue(image.get("url"));
                }

                videos.putIfAbsent(
                        videoId,
                        new VideoDTO(
                                title,
                                "https://www.youtube.com/watch?v="
                                        + videoId,
                                imageUrl
                        )
                );

                if (videos.size() >= 5) {
                    break;
                }
            }

            return new ArrayList<>(videos.values());

        } catch (Exception e) {
            System.err.println(
                    "YouTube API error: "
                    + e.getClass().getSimpleName()
            );

            return List.of();
        }
    }
}
