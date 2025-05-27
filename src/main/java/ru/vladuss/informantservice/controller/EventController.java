package ru.vladuss.informantservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.informantservice.entity.Event;
import ru.vladuss.informantservice.repository.EventRepository;
import ru.vladuss.informantservice.service.impl.EventServiceImpl;
import ru.vladuss.informantservice.util.IcsExportUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventServiceImpl service;

    private final EventRepository eventRepository;

    public EventController(EventServiceImpl service, EventRepository eventRepository) {
        this.service = service;
        this.eventRepository = eventRepository;
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


    @GetMapping(value = "/{id}/ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<byte[]> exportOne(@PathVariable UUID id) {
        Event event = service.getById(id);
        byte[] body = IcsExportUtil.toBytes(List.of(event));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"event-" + id + ".ics\"")
                .body(body);
    }

    @GetMapping(value = "/ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<byte[]> exportAll() {
        List<Event> events = service.getAllForExport();
        byte[] body = IcsExportUtil.toBytes(events);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"events.ics\"")
                .body(body);

    }

}
