package ru.vladuss.integrationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VkWallResponse {
    private Response response;
    public Response getResponse() { return response; }
    public void setResponse(Response response) { this.response = response; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Item> items;
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long id;
        private String text;
        private Long date;
        private List<Attachment> attachments;
        private Integer is_pinned;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public Long getDate() { return date; }
        public void setDate(Long date) { this.date = date; }

        public List<Attachment> getAttachments() { return attachments; }
        public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }

        public Integer getIs_pinned() { return is_pinned; }
        public void setIs_pinned(Integer is_pinned) { this.is_pinned = is_pinned; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        private String type;
        private Photo photo;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Photo getPhoto() { return photo; }
        public void setPhoto(Photo photo) { this.photo = photo; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photo {
        private List<Size> sizes;

        public List<Size> getSizes() { return sizes; }
        public void setSizes(List<Size> sizes) { this.sizes = sizes; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Size {
            private int width;
            private int height;
            private String url;

            public int getWidth() { return width; }
            public void setWidth(int width) { this.width = width; }

            public int getHeight() { return height; }
            public void setHeight(int height) { this.height = height; }

            public String getUrl() { return url; }
            public void setUrl(String url) { this.url = url; }
        }
    }
}
