package com.advertisement.dtos;

import java.util.List;

public class Ads {

    private List<String> tags;
    private String imageUrl;
    private String adId;
    private String targetUrl;

    public Ads() {
    }

    public Ads(List<String> tags, String imageUrl, String adId, String targetUrl) {
        this.tags = tags;
        this.imageUrl = imageUrl;
        this.adId = adId;
        this.targetUrl = targetUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public String toString() {
        return "Ads{" +
            "tags=" + tags +
            ", imageUrl='" + imageUrl + '\'' +
            ", adId='" + adId + '\'' +
            ", targetUrl='" + targetUrl + '\'' +
            '}';
    }
}
