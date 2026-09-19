package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

public class CommentDto {

    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;

    public CommentDto() {
    }

    public CommentDto(
            Long id,
            String text,
            String authorName,
            LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
