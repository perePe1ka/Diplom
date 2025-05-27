package ru.vladuss.informantservice.service;

import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import ru.vladuss.informantservice.entity.Event;

import java.util.List;
import java.util.UUID;

public interface EventService {
    @Timed("events.findAll")
    @Cacheable("events")
    List<Event> getAll();

    @Timed("events.findById")
    @Cacheable(value = "event", key = "#id")
    Event getById(UUID id);

    @Timed("events.create")
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    Event create(Event event);

    @Timed("events.update")
    @Transactional
    @CacheEvict(value = { "events", "event" }, key = "#id")
    Event update(UUID id, Event event);

    @Transactional
    @CacheEvict(value = {"events", "event"}, key = "#id", allEntries = false)
    void delete(UUID id);

    List<Event> getAllForExport();
}
