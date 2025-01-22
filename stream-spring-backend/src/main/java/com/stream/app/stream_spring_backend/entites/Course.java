package com.stream.app.stream_spring_backend.entites;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
public class Course {

    @Id
    private String courseId;

    private String title;

//    @OneToMany(mappedBy = "video")
//    private List<Video> videoList = new ArrayList<>();
}
