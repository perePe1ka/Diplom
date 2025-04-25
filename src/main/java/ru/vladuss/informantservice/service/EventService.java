package ru.vladuss.informantservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.informantservice.entity.Event;
import ru.vladuss.informantservice.repository.EventRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    @Cacheable("events")
    public List<Event> getAll() {
        log.debug("Fetching ALL events from DB");
        List<Event> list = repo.findAll();
        log.info("Found {} events", list.size());
        return list;
    }

    @Cacheable(value = "event", key = "#id")
    public Event getById(UUID id) {
        log.debug("Fetching event id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event {} not found", id);
                    return new RuntimeException("Не найдено событие " + id);
                });
    }

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event create(Event event) {
        log.info("Creating event {}", event);
        Event saved = repo.save(event);
        log.debug("Event saved id={}", saved.getId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = { "events", "event" }, key = "#id", allEntries = false)
    public Event update(UUID id, Event event) {
        log.info("Updating event id={}", id);
        Event exist = getById(id);
        exist.setTitle(event.getTitle());
        exist.setDescription(event.getDescription());
        exist.setStartsAt(event.getStartsAt());
        exist.setEndsAt(event.getEndsAt());
        exist.setLocation(event.getLocation());
        Event updated = repo.save(exist);
        log.debug("Event id={} updated", id);
        return updated;
    }

    @Transactional
    @CacheEvict(value = { "events", "event" }, key = "#id", allEntries = false)
    public void delete(UUID id) {
        log.info("Deleting event id={}", id);
        repo.deleteById(id);
        log.debug("Event id={} deleted", id);
    }
}
