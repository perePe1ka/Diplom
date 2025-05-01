package ru.vladuss.apigateway.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.vladuss.apigateway.config.RabbitConfig;
import ru.vladuss.apigateway.dto.StatementCreateDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@CrossOrigin(
        origins = "http://localhost:5173",
        exposedHeaders = "Content-Disposition")
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private final RabbitTemplate rabbit;

    @Autowired
    public GatewayProxyController(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @PostMapping
    public ResponseEntity<byte[]> create(@Valid @RequestBody StatementCreateDto dto) {

        log.debug("HTTP POST /statements  payload={}", dto);

        Object reply = rabbit.convertSendAndReceive(RabbitConfig.DOC_QUEUE, dto);

        if (reply == null) {
            log.error("Rabbit RPC timeout – не дождался PDF");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        if (!(reply instanceof byte[] pdf)) {
            log.error("Rabbit RPC вернул {}, ожидался byte[]", reply.getClass());
            throw new IllegalStateException("Ожидал byte[], а получил " + reply.getClass());
        }

        String fileName = dto.type().name().toLowerCase() + "-" + UUID.randomUUID() + ".pdf";
        log.info("PDF {} байт сформирован, отдаю файл {}", pdf.length, fileName);

        HttpHeaders hdr = new HttpHeaders();
        hdr.setContentType(MediaType.APPLICATION_PDF);
        hdr.setContentDisposition(ContentDisposition
                .attachment()
                .filename(fileName)
                .build());
        hdr.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");

        return new ResponseEntity<>(pdf, hdr, HttpStatus.OK);
    }
}
