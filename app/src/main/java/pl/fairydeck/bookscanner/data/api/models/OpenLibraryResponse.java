package pl.fairydeck.bookscanner.data.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class OpenLibraryResponse {
    @SerializedName("title")
    private String title;
    
    @SerializedName("authors")
    private List<Author> authors;
    
    @SerializedName("publishers")
    private List<String> publishers;
    
    @SerializedName("publish_date")
    private String publishDate;
    
    @SerializedName("description")
    private Object description; // Can be String or Map
    
    @SerializedName("isbn_10")
    private List<String> isbn10;
    
    @SerializedName("isbn_13")
    private List<String> isbn13;
    
    @SerializedName("covers")
    private List<Long> covers;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public String getDescriptionText() {
        if (description instanceof String) {
            return (String) description;
        } else if (description instanceof Map) {
            Map<String, Object> descMap = (Map<String, Object>) description;
            Object value = descMap.get("value");
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    public List<String> getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(List<String> isbn10) {
        this.isbn10 = isbn10;
    }

    public List<String> getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(List<String> isbn13) {
        this.isbn13 = isbn13;
    }

    public List<Long> getCovers() {
        return covers;
    }

    public void setCovers(List<Long> covers) {
        this.covers = covers;
    }

    public String getCoverImageUrl() {
        if (covers != null && !covers.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + covers.get(0) + "-L.jpg";
        }
        return null;
    }

    public static class Author {
        @SerializedName("key")
        private String key;
        
        @SerializedName("name")
        private String name;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}





