package ru.vladuss.integrationservice.dto;

import java.io.Serializable;

public class SimplePostDto implements Serializable {
    private String text;
    private String photo;
    private String date;
    private String url;

    public SimplePostDto(String text, String photo, String date, String url) {
        this.text = text;
        this.photo = photo;
        this.date = date;
        this.url = url;
    }

    public SimplePostDto() {
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}