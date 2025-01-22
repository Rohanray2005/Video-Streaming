package com.stream.app.stream_spring_backend.service.impl;

import com.stream.app.stream_spring_backend.entites.Video;
import com.stream.app.stream_spring_backend.repositories.VideoRepository;
import com.stream.app.stream_spring_backend.service.VideoService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    private String DIR;

    @Value("${file.video.hls}")
    String HLS_DIR;

    private final VideoRepository videoRepository;

    @PostConstruct
    public void init() {
        File file = new File(DIR);
        File file1 = new File(HLS_DIR);

        if(!file.exists()) {
            file.mkdir();
        }
        if(!file1.exists()) {
            file1.mkdir();
        }
    }

    @Override
    public Video saveVideo(Video video, MultipartFile file) {
        try{
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream stream = file.getInputStream();

            String cleanFileName = StringUtils.cleanPath(fileName);

            Path path = Paths.get(DIR, cleanFileName);

            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);
            video.setFilePath(path.toString());

            videoRepository.save(video);

            //processing video
            processVideo(video.getVideoId());

            return video;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    @Cacheable("config")
    public Video getVideoById(String Id) {
        System.out.println("cache miss !!");
        return videoRepository.findById(Id).orElseThrow(()->new RuntimeException("Video Not Found !!"));
    }

    @Override
    public Video getVideoByTitle(String title) {
        return null;
    }

    @Override
    public Video getAll() {
        return null;
    }

    private void processVideo(String videoId) {
        Video video = this.getVideoById(videoId);
        String filePath = video.getFilePath();
        //path where to store data:
        Path videoPath = Paths.get(filePath);
        try {
            // ffmpeg command
            Path outputPath = Paths.get(HLS_DIR, videoId);

            Files.createDirectories(outputPath);


            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );
            System.out.println(ffmpegCmd);
            //file this command
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Video processing fail!!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
