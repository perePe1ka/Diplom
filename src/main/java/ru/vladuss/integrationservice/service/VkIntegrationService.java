package ru.vladuss.integrationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.vladuss.integrationservice.dto.SimplePostDto;
import ru.vladuss.integrationservice.dto.VkWallResponse;
import ru.vladuss.integrationservice.entity.PostEntity;
import ru.vladuss.integrationservice.repository.PostRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VkIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(VkIntegrationService.class);

    private final RestTemplate restTemplate;
    private final PostRepository postRepository;
    private final PostPersistenceService postPersistenceService;
    private final String groupId;
    private final String apiVersion;
    private final String accessToken;

    private final ZoneId zone = ZoneId.systemDefault();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public VkIntegrationService(
            RestTemplate restTemplate,
            PostRepository postRepository,
            PostPersistenceService postPersistenceService,
            @Value("${integration.vk.group-id}") String groupId,
            @Value("${integration.vk.api-version}") String apiVersion,
            @Value("${integration.vk.access-token}") String accessToken
    ) {
        this.restTemplate = restTemplate;
        this.postRepository = postRepository;
        this.postPersistenceService = postPersistenceService;
        this.groupId = groupId;
        this.apiVersion = apiVersion;
        this.accessToken = accessToken;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "vk_simple",
            key = "#offset + '_' + #limit",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<SimplePostDto> fetchSimple(int offset, int limit) {
        try {
            log.info("Попытка получить посты из VK API. Offset: {}, Limit: {}", offset, limit);
            String url = buildUrl(offset, limit);
            VkWallResponse resp = execute(url);
            List<SimplePostDto> posts = mapToDto(resp);

            if (!posts.isEmpty()) {
                log.info("Успешно получено {} постов из VK API. Попытка сохранения в базу данных (только текст).", posts.size());
                postPersistenceService.savePostsToDb(posts);
            } else {
                log.warn("VK API не вернул постов. Попытка получить из базы данных.");
                return fetchFromDb(offset, limit);
            }
            return posts;
        } catch (RestClientException e) {
            log.error("Не удалось получить данные из VK API из-за RestClientException: {}. Загрузка из БД.", e.getMessage());
            return fetchFromDb(offset, limit);
        } catch (Exception e) {
            log.error("Произошла непредвиденная ошибка при взаимодействии с VK API или при сохранении данных: {}. Загрузка из БД.", e.getMessage(), e);
            return fetchFromDb(offset, limit);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    protected List<SimplePostDto> fetchFromDb(int offset, int limit) {
        log.info("Получение постов из базы данных (в новой транзакции). Offset: {}, Limit: {}", offset, limit);
        int pageNumber = offset / limit;
        PageRequest pageable = PageRequest.of(pageNumber, limit);
        List<PostEntity> entities = postRepository.findAllByOrderByDateDesc(pageable);

        log.info("Найдено {} постов в базе данных.", entities.size());
        return entities.stream()
                .map(entity -> new SimplePostDto(entity.getText(), null, entity.getDate(), entity.getUrl()))
                .collect(Collectors.toList());
    }

    private String buildUrl(int offset, int limit) {
        String ownerId = "-" + groupId;
        return UriComponentsBuilder.newInstance()
                .scheme("https").host("api.vk.com").path("/method/wall.get")
                .queryParam("owner_id", ownerId)
                .queryParam("access_token", accessToken)
                .queryParam("v", apiVersion)
                .queryParam("count", limit)
                .queryParam("offset", offset)
                .build().toUriString();
    }

    private VkWallResponse execute(String url) {
        log.debug("Выполнение запроса к VK API: {}", url);
        VkWallResponse response = restTemplate.getForObject(url, VkWallResponse.class);
        if (response == null || response.getResponse() == null) {
            throw new RestClientException("VK API вернул null или пустой объект ответа для URL: " + url);
        }
        return response;
    }

    private List<SimplePostDto> mapToDto(VkWallResponse resp) {
        if (resp.getResponse() == null || resp.getResponse().getItems() == null) {
            log.warn("Ответ VK API не содержит поля 'response' или массива 'items'.");
            return Collections.emptyList();
        }

        List<SimplePostDto> out = new ArrayList<>();
        for (VkWallResponse.Item it : resp.getResponse().getItems()) {
            if (it.getIs_pinned() != null && it.getIs_pinned() == 1) {
                continue;
            }

            String text = Optional.ofNullable(it.getText()).orElse("");
            String photo = extractLargestPhoto(it.getAttachments());

            String dateStr;
            if (it.getDate() != null) {
                try {
                    dateStr = LocalDateTime.ofInstant(Instant.ofEpochSecond(it.getDate()), zone).format(fmt);
                } catch (DateTimeParseException e) {
                    log.warn("Ошибка форматирования даты для поста с id {}: {}. Используется 'Неизвестная дата'.", (it.getId() != null ? it.getId().toString() : "N/A"), e.getMessage());
                    dateStr = "Неизвестная дата";
                }
            } else {
                log.warn("Пост VK (id: {}) имеет null в поле 'date'. Используется 'Неизвестная дата'.", (it.getId() != null ? it.getId().toString() : "N/A"));
                dateStr = "Неизвестная дата";
            }

            String link;
            if (it.getId() != null) {
                link = "https://vk.com/wall-" + groupId + "_" + it.getId();
            } else {
                log.warn("Пост VK имеет null в поле 'id'. Невозможно сгенерировать ссылку. Используется 'Неизвестная ссылка'.");
                link = "Неизвестная ссылка";
            }
            out.add(new SimplePostDto(text, photo, dateStr, link));
        }

        log.info("Ответ VK API преобразован в {} DTO.", out.size());
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