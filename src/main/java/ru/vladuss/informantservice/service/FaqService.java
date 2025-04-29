package ru.vladuss.informantservice.service;

import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.informantservice.entity.Faq;

import java.util.List;
import java.util.UUID;

public interface FaqService {
    @Timed("faqs.findAll")
    @Cacheable("faqs")
    List<Faq> getAll();

    @Timed("faqs.findById")
    @Cacheable(value = "faq", key = "#id")
    Faq getById(UUID id);

    @Timed("faqs.create")
    @Transactional
    @CacheEvict(value = "faqs", allEntries = true)
    Faq create(Faq faq);

    @Timed("faqs.update")
    @Transactional
    @CacheEvict(value = { "faqs", "faq" }, key = "#id")
    Faq update(UUID id, Faq faq);

    @Timed("faqs.delete")
    @Transactional
    @CacheEvict(value = { "faqs", "faq" }, key = "#id")
    void delete(UUID id);
}
