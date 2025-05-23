package ru.vladuss.integrationservice.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class SimplePostDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private String photo;
    private String date;
    private String url;
    private List<String> tags;

    public SimplePostDto() {}

    public SimplePostDto(String text, String photo, String date, String url) {
        this.text = text;
        this.photo = photo;
        this.date = date;
        this.url = url;
    }



    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimplePostDto that)) return false;
        return Objects.equals(text, that.text)
                && Objects.equals(photo, that.photo)
                && Objects.equals(date, that.date)
                && Objects.equals(url, that.url)
                && Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, photo, date, url, tags);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SimplePostDto.class.getSimpleName() + "[", "]")
                .add("text='" + text + "'")
                .add("photo='" + photo + "'")
                .add("date='" + date + "'")
                .add("url='" + url + "'")
                .add("tags=" + tags)
                .toString();
    }


}
