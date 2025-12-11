package com.ensao.mytime.home.model;
import com.google.gson.annotations.SerializedName;

public class Quote {

    @SerializedName("q")
    private final String text;

    @SerializedName("a")
    private final String author;

    private final String category = "Motivation";

    public Quote(String text, String author) {
        this.text = text;
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }
}