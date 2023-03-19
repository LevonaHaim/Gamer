package com.gamerzone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Game {
    String id;
    @SerializedName("name")
    String name;
    @SerializedName("released")
    String releaseDate;
    @SerializedName("background_image")
    String imageUrl;
    @SerializedName("rating")
    Float rating;
    @SerializedName("genres")
    List<Genre> genres;

    public Game(String name, String releaseDate, String imageUrl, Float rating, List<Genre> genres) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.genres = genres;
    }

    public Game(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Game() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }
}

