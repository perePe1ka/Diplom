package ru.vladuss.integrationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.vladuss.integrationservice.dto.SimplePostDto;
import ru.vladuss.integrationservice.dto.VkWallResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class VkIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(VkIntegrationService.class);

    private final RestTemplate restTemplate;
    private final String groupId;
    private final String apiVersion;
    private final String accessToken;

    private final ZoneId zone = ZoneId.systemDefault();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public VkIntegrationService(
            RestTemplate restTemplate,
            @Value("${integration.vk.group-id}") String groupId,
            @Value("${integration.vk.api-version}") String apiVersion,
            @Value("${integration.vk.access-token}") String accessToken
    ) {
        this.restTemplate = restTemplate;
        this.groupId      = groupId;
        this.apiVersion   = apiVersion;
        this.accessToken  = accessToken;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value  = "vk_simple",
            key    = "#offset + '_' + #limit",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<SimplePostDto> fetchSimple(int offset, int limit) {
        String url = buildUrl(offset, limit);
        VkWallResponse resp = executeWithRetry(url, 3);
        return mapToDto(resp, offset, limit);
    }

    private String buildUrl(int offset, int limit) {
        String ownerId = "-" + groupId;
        return UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/wall.get")
                .queryParam("owner_id", ownerId)
                .queryParam("access_token", accessToken)
                .queryParam("v", apiVersion)
                .queryParam("count",  limit)
                .queryParam("offset", offset)
                .build().toUriString();
    }

    private VkWallResponse executeWithRetry(String url, int attempts) {
        int attempt = 0;
        RestClientException lastEx = null;

        while (attempt < attempts) {
            try {
                attempt++;
                log.debug("VK API attempt {} url={}", attempt, url);
                return restTemplate.getForObject(url, VkWallResponse.class);
            } catch (RestClientException ex) {
                lastEx = ex;
                log.warn("VK API call failed attempt {}", attempt);
                sleepRandomBackoff(attempt);
            }
        }
        throw new IllegalStateException("VK API failed after " + attempts + " attempts", lastEx);
    }

    private void sleepRandomBackoff(int attempt) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(200L * attempt, 400L * attempt));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private List<SimplePostDto> mapToDto(VkWallResponse resp, int offset, int limit) {
        if (resp == null || resp.getResponse() == null) {
            log.warn("VK API returned empty response");
            return Collections.emptyList();
        }

        List<SimplePostDto> out = new ArrayList<>(limit);

        for (VkWallResponse.Item it : resp.getResponse().getItems()) {
            if (it.getIs_pinned() != null && it.getIs_pinned() == 1) {
                continue;
            }

            String text  = Optional.ofNullable(it.getText()).orElse("");
            String photo = extractLargestPhoto(it.getAttachments());
            String date  = LocalDateTime.ofInstant(Instant.ofEpochSecond(it.getDate()), zone).format(fmt);
            String link  = "https://vk.com/wall-" + groupId + "_" + it.getId();
            out.add(new SimplePostDto(text, photo, date, link));
        }

        log.info("VK API fetched {} posts (offset={}, limit={})", out.size(), offset, limit);
        return out;
    }

    private String extractLargestPhoto(List<VkWallResponse.Attachment> attachments) {
        return Optional.ofNullable(attachments)
                .flatMap(atts -> atts.stream()
                        .filter(a -> "photo".equals(a.getType()) && a.getPhoto() != null)
                        .findFirst()
                        .flatMap(a -> a.getPhoto().getSizes().stream()
                                .max(Comparator.comparingInt(VkWallResponse.Photo.Size::getWidth))
                                .map(VkWallResponse.Photo.Size::getUrl)))
                .orElse(null);
    }
}
