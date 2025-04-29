package ru.vladuss.informantservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.informantservice.entity.Faq;
import ru.vladuss.informantservice.service.impl.FaqServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/faqs")
public class FaqController {

    private static final Logger log = LoggerFactory.getLogger(FaqController.class);

    private final FaqServiceImpl service;

    public FaqController(FaqServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public List<Faq> getAll() {
        log.debug("HTTP GET /faqs");
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Faq get(@PathVariable UUID id) {
        log.debug("HTTP GET /faqs/{}", id);
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Faq create(@RequestBody Faq faq) {
        log.debug("HTTP POST /faqs payload={}", faq);
        return service.create(faq);
    }

    @PutMapping("/{id}")
    public Faq update(@PathVariable UUID id, @RequestBody Faq faq) {
        log.debug("HTTP PUT /faqs/{} payload={}", id, faq);
        return service.update(id, faq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        log.debug("HTTP DELETE /faqs/{}", id);
        service.delete(id);
    }
}
