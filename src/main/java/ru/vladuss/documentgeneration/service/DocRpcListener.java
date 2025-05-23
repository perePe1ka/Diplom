package ru.vladuss.documentgeneration.service;

import org.slf4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vladuss.documentgeneration.config.RabbitConfig;
import ru.vladuss.documentgeneration.dto.StatementCreateDto;


@Component
public class DocRpcListener {

    private final PdfService pdfService;
    private final GoogleSheetsService sheets;
    private final Logger log;

    @Autowired
    public DocRpcListener(PdfService pdfService,
                          GoogleSheetsService sheets, Logger log) {
        this.pdfService = pdfService;
        this.sheets = sheets;
        this.log = log;
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
