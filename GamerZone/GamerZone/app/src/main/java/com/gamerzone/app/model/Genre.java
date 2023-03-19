package com.gamerzone.app.model;

import com.google.gson.annotations.SerializedName;

public class Genre {
    @SerializedName("name")
    String name;

    public Genre() {

    }

    public Genre(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
