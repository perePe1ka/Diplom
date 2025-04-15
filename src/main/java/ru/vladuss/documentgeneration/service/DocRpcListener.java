package ru.vladuss.documentgeneration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vladuss.documentgeneration.config.RabbitConfig;
import ru.vladuss.documentgeneration.dto.StatementCreateDto;

@Slf4j
@Component
public class DocRpcListener {

    private final PdfService pdfService;
    private final GoogleSheetsService sheets;

    @Autowired
    public DocRpcListener(PdfService pdfService,
                          GoogleSheetsService sheets) {
        this.pdfService = pdfService;
        this.sheets     = sheets;
    }

    @RabbitListener(queues = RabbitConfig.DOC_QUEUE)
    public byte[] handle(StatementCreateDto dto) throws Exception {

        log.info("Received DOC RPC request {}", dto);

        long start = System.currentTimeMillis();

        sheets.appendRow(dto);
        byte[] pdf = pdfService.generate(dto);

        log.info("DOC RPC processed in {} ms (PDF {} bytes)",
                System.currentTimeMillis() - start, pdf.length);

        return pdf;
    }
}
