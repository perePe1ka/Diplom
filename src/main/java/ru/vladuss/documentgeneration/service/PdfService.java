package ru.vladuss.documentgeneration.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;                    // ← логгер
import org.springframework.stereotype.Service;
import ru.vladuss.documentgeneration.constants.StatementType;
import ru.vladuss.documentgeneration.dto.StatementCreateDto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PdfService {

    private final Path storageDir =
            Path.of(System.getProperty("user.home"), "Downloads", "ProfcomDocs");
    private final Configuration fm =
            new Configuration(Configuration.VERSION_2_3_32);

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(storageDir);
        fm.setClassForTemplateLoading(getClass(), "/templates");
        fm.setDefaultEncoding("UTF-8");
        log.info("PDF-service initialized. Storage dir={}", storageDir);
    }

    public byte[] generate(@NotNull StatementCreateDto dto) throws Exception {

        log.debug("Start PDF generation for {}", dto);

        StatementType type = dto.type();
        String ftl = type == StatementType.AID ? "aid.ftl" : "ticket.ftl";
        log.debug("Chosen template={}", ftl);

        Template tpl = fm.getTemplate(ftl);
        StringWriter html = new StringWriter();
        tpl.process(
                Map.of("form", dto,
                        "now", LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                html);
        log.debug("HTML prepared ({} bytes)", html.getBuffer().length());

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32_000);
        new PdfRendererBuilder()
                .useFastMode()
                .withHtmlContent(html.toString(), null)
                .toStream(baos)
                .run();
        log.debug("PDF rendered ({} bytes)", baos.size());

        String fileName = type.name().toLowerCase() + "-" + UUID.randomUUID() + ".pdf";
        try (OutputStream fileOut =
                     Files.newOutputStream(storageDir.resolve(fileName))) {
            baos.writeTo(fileOut);
            log.info("PDF saved to disk as {}", fileName);
        }

        return baos.toByteArray();
    }
}
