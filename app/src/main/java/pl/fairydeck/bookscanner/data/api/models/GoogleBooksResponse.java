package pl.fairydeck.bookscanner.data.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoogleBooksResponse {
    @SerializedName("items")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public VolumeInfo getFirstVolumeInfo() {
        if (items != null && !items.isEmpty() && items.get(0).getVolumeInfo() != null) {
            return items.get(0).getVolumeInfo();
        }
        return null;
    }

    public static class Item {
        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;

        public VolumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(VolumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
        }
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;
        
        @SerializedName("authors")
        private List<String> authors;
        
        @SerializedName("publisher")
        private String publisher;
        
        @SerializedName("publishedDate")
        private String publishedDate;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("industryIdentifiers")
        private List<IndustryIdentifier> industryIdentifiers;
        
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getAuthor() {
            if (authors != null && !authors.isEmpty()) {
                return String.join(", ", authors);
            }
            return null;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<IndustryIdentifier> getIndustryIdentifiers() {
            return industryIdentifiers;
        }

        public void setIndustryIdentifiers(List<IndustryIdentifier> industryIdentifiers) {
            this.industryIdentifiers = industryIdentifiers;
        }

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }

        public String getCoverImageUrl() {
            if (imageLinks != null && imageLinks.getThumbnail() != null) {
                return imageLinks.getThumbnail().replace("http://", "https://");
            }
            return null;
        }
    }

    public static class IndustryIdentifier {
        @SerializedName("type")
        private String type;
        
        @SerializedName("identifier")
        private String identifier;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;
        
        @SerializedName("smallThumbnail")
        private String smallThumbnail;

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getSmallThumbnail() {
            return smallThumbnail;
        }

        public void setSmallThumbnail(String smallThumbnail) {
            this.smallThumbnail = smallThumbnail;
        }
    }
}





