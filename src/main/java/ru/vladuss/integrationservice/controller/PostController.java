package ru.vladuss.integrationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.integrationservice.dto.PageResponse;
import ru.vladuss.integrationservice.dto.SimplePostDto;
import ru.vladuss.integrationservice.service.VkIntegrationService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final VkIntegrationService vkService;

    public PostController(VkIntegrationService vkService) {
        this.vkService = vkService;
    }

    @GetMapping("/simple")
    public ResponseEntity<List<SimplePostDto>> getSimple(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "6") int limit) {

        limit = sanitizeLimit(limit);
        offset = Math.max(offset, 0);

        log.debug("HTTP GET /posts/simple offset={} limit={}", offset, limit);

        List<SimplePostDto> list = vkService.fetchSimple(offset, limit);

        log.info("Respond /posts/simple size={}", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/simple/page")
    public ResponseEntity<PageResponse<SimplePostDto>> fetchSimple(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "6") int limit) {

        List<SimplePostDto> items = vkService.fetchSimple(offset, limit);
        PageResponse<SimplePostDto> page = new PageResponse<>(items, offset, limit);
        return ResponseEntity.ok(page);
    }

    private int sanitizeLimit(int limit) {
        return Math.min(Math.max(limit, 1), 100);
    }


}
