package ru.vladuss.integrationservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vladuss.integrationservice.entity.PostEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findByUrl(String url);
    List<PostEntity> findAllByOrderByDateDesc(Pageable pageable);
}