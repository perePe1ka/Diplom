package ru.vladuss.documentgeneration.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.vladuss.documentgeneration.dto.StatementCreateDto;

import java.io.IOException;
import java.util.List;


@Service
public class GoogleSheetsService {

    private final Sheets sheets;
    private final String sheetId;
    private final Logger log;

    public GoogleSheetsService(@Value("${google.sheet.id}") String sheetId,
                               @Value("${google.creds}") Resource credentialsPath, Logger log) throws Exception {

        this.sheetId = sheetId;

        log.info("Initializing Google Sheets client (sheetId={})", sheetId);

        GoogleCredentials creds = GoogleCredentials
                .fromStream(credentialsPath.getInputStream())
                .createScoped(List.of(SheetsScopes.SPREADSHEETS));

        this.sheets = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(creds))
                .setApplicationName("docgen")
                .build();

        log.info("Google Sheets client ready");

        this.log = log;
    }

    private static String sheetName(StatementCreateDto dto) {
        return switch (dto.type()) {
            case TICKET -> "Ticket";
            case AID -> "Aid";
        };
    }

    public void appendRow(StatementCreateDto dto) {

        String targetSheet = sheetName(dto);
        List<Object> row = List.of(
                dto.lastName(), dto.firstName(), dto.middleName(),
                dto.email(), dto.phone(),
                dto.groupOrPosition(), dto.type().name()
        );

        log.debug("Appending row to sheet={} data={}", targetSheet, row);

        try {
            sheets.spreadsheets().values()
                    .append(sheetId, targetSheet + "!A1",
                            new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("RAW")
                    .execute();

            log.info("Row successfully written to Google Sheets");
        } catch (IOException e) {
            log.error("Failed to write to Google Sheets", e);
            throw new RuntimeException("Не удалось записать в Google Sheets", e);
        }
    }
}
