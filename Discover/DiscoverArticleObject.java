package com.arsinex.com.Discover;

import android.os.Environment;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoverArticleObject {

    private String date, link, title, content, summary, author_id, author_url, image_url;
    private String author = null;

    /**
     *
     * @param date
     * @param link
     * @param title
     * @param content
     * @param summary
     * @param author_id
     * @param author_url
     */

    public DiscoverArticleObject(String date, String link, String title, String content, String summary, String author_id, String author_url) {
        this.date = date;
        this.link = link;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author_id = author_id;
        this.author_url = author_url;
    }

    public String getDate() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getAuthor_url() {
        return author_url;
    }

    public String getAuthor() {
        return author;
    }

    public String getImage_url() {
        String imgRegex = "(?i)<img[^>]+?src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(content);

        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
