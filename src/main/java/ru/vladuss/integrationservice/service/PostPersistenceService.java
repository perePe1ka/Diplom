package ru.vladuss.integrationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.integrationservice.dto.SimplePostDto;
import ru.vladuss.integrationservice.entity.PostEntity;
import ru.vladuss.integrationservice.repository.PostRepository;

import java.util.List;

@Service
public class PostPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(PostPersistenceService.class);
    private final PostRepository postRepository;

    public PostPersistenceService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePostsToDb(List<SimplePostDto> posts) {
        int newPostsCount = 0;
        for (SimplePostDto dto : posts) {
            if (postRepository.findByUrl(dto.getUrl()).isEmpty()) {
                PostEntity entity = new PostEntity();
                entity.setText(dto.getText());
                entity.setDate(dto.getDate());
                entity.setUrl(dto.getUrl());
                postRepository.save(entity);
                newPostsCount++;
            }
        }
        if (newPostsCount > 0) {
            log.info("Сохранено {} новых текстовых постов в базу данных.", newPostsCount);
        } else {
            log.info("Новых постов для сохранения в базу данных не найдено.");
        }
    }
}