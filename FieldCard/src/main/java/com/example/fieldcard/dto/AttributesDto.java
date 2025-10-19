package com.example.fieldcard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributesDto {
    private String title;
    private String link;
    private OffsetDateTime modified;
    private String format;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public OffsetDateTime getModified() { return modified; }
    public void setModified(OffsetDateTime modified) { this.modified = modified; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
