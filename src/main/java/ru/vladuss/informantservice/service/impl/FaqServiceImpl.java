package ru.vladuss.informantservice.service.impl;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.informantservice.entity.Faq;
import ru.vladuss.informantservice.repository.FaqRepository;
import ru.vladuss.informantservice.service.FaqService;

import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class FaqServiceImpl implements FaqService {

    private static final Logger log = LoggerFactory.getLogger(FaqServiceImpl.class);

    private final FaqRepository repo;

    public FaqServiceImpl(FaqRepository repo) {
        this.repo = repo;
    }

    @Timed("faqs.findAll")
    @Cacheable("faqs")
    @Override
    public List<Faq> getAll() {
        log.debug("Fetching ALL FAQs from DB");
        List<Faq> list = repo.findAll();
        log.info("Found {} FAQs", list.size());
        return list;
    }

    @Timed("faqs.findById")
    @Cacheable(value = "faq", key = "#id")
    @Override
    public Faq getById(UUID id) {
        log.debug("Fetching FAQ id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("FAQ {} not found", id);
                    return new RuntimeException("Не найден FAQ " + id);
                });
    }

    @Timed("faqs.create")
    @Transactional
    @CacheEvict(value = "faqs", allEntries = true)
    @Override
    public Faq create(Faq faq) {
        log.info("Creating FAQ {}", faq);
        Faq saved = repo.save(faq);
        log.debug("FAQ saved with id={}", saved.getId());
        return saved;
    }

    @Timed("faqs.update")
    @Transactional
    @CacheEvict(value = {"faqs", "faq"}, key = "#id")
    @Override
    public Faq update(UUID id, Faq faq) {
        log.info("Updating FAQ id={}", id);
        Faq exist = getById(id);
        exist.setQuestion(faq.getQuestion());
        exist.setAnswer(faq.getAnswer());
        Faq updated = repo.save(exist);
        log.debug("FAQ id={} updated", id);
        return updated;
    }

    @Timed("faqs.delete")
    @Transactional
    @CacheEvict(value = {"faqs", "faq"}, key = "#id")
    @Override
    public void delete(UUID id) {
        log.info("Deleting FAQ id={}", id);
        repo.deleteById(id);
        log.debug("FAQ id={} deleted", id);
    }
}
