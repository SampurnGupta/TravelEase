package com.example.travelease.model;

public class Destination {
    private String name;
    private String country;
    private String imageUrl;

    public Destination(String name, String country, String imageUrl) {
        this.name = name;
        this.country = country;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
