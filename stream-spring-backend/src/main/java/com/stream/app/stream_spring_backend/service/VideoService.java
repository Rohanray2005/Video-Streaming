package com.stream.app.stream_spring_backend.service;

import com.stream.app.stream_spring_backend.entites.Video;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {

    // save
    Video saveVideo(Video video, MultipartFile file);

    //get video by Id
    Video getVideoById(String Id);

    // get video by title
    Video getVideoByTitle(String title);

    // get all videos
    Video getAll();
}
