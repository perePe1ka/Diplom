package ru.vladuss.integrationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.integrationservice.dto.SimplePostDto;
import ru.vladuss.integrationservice.service.VkIntegrationService;

import java.util.List;

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

        limit = Math.min(Math.max(limit, 1), 100);
        log.debug("HTTP GET /posts/simple  offset={}  limit={}", offset, limit);

        List<SimplePostDto> list = vkService.fetchSimple(offset, limit);
        log.info("Respond /posts/simple  size={}", list.size());

        return ResponseEntity.ok(list);
    }
}
