package ru.vladuss.documentgeneration.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PdfService {

    private enum OutputTarget {DISK, MEMORY, BOTH}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TS_FMT   = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Path           storageDir =
            Path.of(System.getProperty("user.home"), "Downloads", "ProfcomDocs");
    private final Configuration  fm         =
            new Configuration(Configuration.VERSION_2_3_32);
    private final Map<StatementType, String> templateCache = new ConcurrentHashMap<>();

    private final Logger log;

    public PdfService(Logger log) {
        this.log = log;
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(storageDir);
        fm.setClassForTemplateLoading(getClass(), "/templates");
        fm.setDefaultEncoding("UTF-8");
        log.info("PDF-service initialized at {}", storageDir.toAbsolutePath());
    }

    public byte[] generate(@NotNull StatementCreateDto dto) throws Exception {
        return generate(dto, OutputTarget.BOTH);
    }

    private byte[] generate(StatementCreateDto dto, OutputTarget mode) throws Exception {

        long t0 = System.nanoTime();

        String html = renderTemplate(dto);
        byte[] pdf  = renderPdf(html);

        if (mode == OutputTarget.DISK || mode == OutputTarget.BOTH) {
            writeToDisk(pdf, dto.type());
        }

        long took = (System.nanoTime() - t0) / 1_000_000;
        log.info("PDF ready ({} bytes, {} ms, mode={})", pdf.length, took, mode);

        return pdf;
    }

    private String renderTemplate(StatementCreateDto dto) throws Exception {

        StatementType type = dto.type();
        String tplName = templateCache.computeIfAbsent(type, k ->
                k == StatementType.AID ? "aid.ftl" : "ticket.ftl");

        Template tpl = fm.getTemplate(tplName);

        StringWriter html = new StringWriter(8_192);
        tpl.process(
                Map.of("form", dto,
                        "now", LocalDateTime.now().format(DATE_FMT)),
                html);

        return html.toString();
    }

    private byte[] renderPdf(String html) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32_000);

        new PdfRendererBuilder()
                .useFastMode()
                .withHtmlContent(html, null)
                .toStream(baos)
                .run();

        return baos.toByteArray();
    }

    private void writeToDisk(byte[] pdf, StatementType type) {
        String file = type.name().toLowerCase() + "-" + TS_FMT.format(LocalDateTime.now())
                + "-" + UUID.randomUUID() + ".pdf";
        try (OutputStream out = Files.newOutputStream(storageDir.resolve(file))) {
            out.write(pdf);
            log.debug("PDF saved as {}", file);
        } catch (IOException e) {
            log.warn("Cannot write PDF to disk", e);
        }
    }

    public byte[] generateMemoryOnly(StatementCreateDto dto) throws Exception {
        return generate(dto, OutputTarget.MEMORY);
    }
    public void generateDiskOnly(StatementCreateDto dto) throws Exception {
        generate(dto, OutputTarget.DISK);
    }
}
