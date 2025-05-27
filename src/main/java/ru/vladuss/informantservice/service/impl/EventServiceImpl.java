package ru.vladuss.informantservice.service.impl;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.informantservice.entity.Event;
import ru.vladuss.informantservice.repository.EventRepository;
import ru.vladuss.informantservice.service.EventService;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository repo;

    public EventServiceImpl(EventRepository repo) {
        this.repo = repo;
    }

    @Timed("events.findAll")
    @Cacheable("events")
    @Override
    public List<Event> getAll() {
        log.debug("Fetching ALL events");
        return repo.findAll();
    }

    @Timed("events.findById")
    @Cacheable(value = "event", key = "#id")
    @Override
    public Event getById(UUID id) {
        log.debug("Fetching event id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event {} not found", id);
                    return new RuntimeException("Не найдено событие " + id);
                });
    }

    @Timed("events.create")
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    @Override
    public Event create(Event event) {
        log.info("Creating event {}", event);
        Event saved = repo.save(event);
        log.debug("Event saved id={}", saved.getId());
        return saved;
    }

    @Timed("events.update")
    @Transactional
    @CacheEvict(value = {"events", "event"}, key = "#id")
    @Override
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
    @CacheEvict(value = {"events", "event"}, key = "#id", allEntries = false)
    @Override
    public void delete(UUID id) {
        log.info("Deleting event id={}", id);
        repo.deleteById(id);
        log.debug("Event id={} deleted", id);
    }


    @Override
    public List<Event> getAllForExport() {
        log.debug("Fetching ALL events for Export");
        return repo.findAll();
    }
}
