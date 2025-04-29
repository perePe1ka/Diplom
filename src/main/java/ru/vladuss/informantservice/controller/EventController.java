package ru.vladuss.informantservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.informantservice.entity.Event;
import ru.vladuss.informantservice.service.impl.EventServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventServiceImpl service;

    public EventController(EventServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public List<Event> getAll() {
        log.debug("HTTP GET /events");
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Event get(@PathVariable UUID id) {
        log.debug("HTTP GET /events/{}", id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@RequestBody Event event) {
        log.debug("HTTP POST /events payload={}", event);
        return service.create(event);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable UUID id, @RequestBody Event event) {
        log.debug("HTTP PUT /events/{} payload={}", id, event);
        return service.update(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        log.debug("HTTP DELETE /events/{}", id);
        service.delete(id);
    }
}
