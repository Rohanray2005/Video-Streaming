package com.stream.app.stream_spring_backend.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "video")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    private String videoId;

    @Column(nullable = false)
    private String videoTitle;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String filePath;
}
