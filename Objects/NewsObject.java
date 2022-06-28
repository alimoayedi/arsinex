package com.arsinex.com.Objects;

public class NewsObject {

    private String imgURL;
    private String header;
    private String source;
    private String date;
    private String news_url;

    public NewsObject(String imgURL, String header, String source, String date, String news_url) {
        this.imgURL = imgURL;
        this.header = header;
        this.source = source;
        this.date = date;
        this.news_url = news_url;
        // TODO: Kind of code might be required to fetch news from server
        // TODO: this.imageStream = imageStream;    is used to display SVG images
    }

    public String getImageURL() {
        return imgURL;
    }

    public String getHeader() {
        return header;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {
        return date;
    }

    public String getNews_url() { return news_url; }
}
