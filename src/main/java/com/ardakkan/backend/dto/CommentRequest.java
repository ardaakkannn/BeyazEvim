package com.ardakkan.backend.dto;

public class CommentRequest {
    private String title;
    private Integer rating;
    private String text;

    // Getter ve Setter'lar
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

