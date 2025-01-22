package com.stream.app.stream_spring_backend.controller;

import com.stream.app.stream_spring_backend.entites.Video;
import com.stream.app.stream_spring_backend.models.CustomMessage;
import com.stream.app.stream_spring_backend.service.VideoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin("*")
public class videoController {

    public static final int CHUNK_SIZE = 1024*1024;

    private final VideoService videoService;

    @GetMapping("/get/{videoId}")
    public Video getVideo(@PathVariable String videoId) {
        return videoService.getVideoById(videoId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file,
                                                @RequestParam("title") String title,
                                                @RequestParam("description") String description) {
        Video video = new Video();
        video.setVideoId(UUID.randomUUID().toString());
        video.setVideoTitle(title);
        video.setDescription(description);

        Video savedVideo = videoService.saveVideo(video, file);
        if(savedVideo != null){
            return ResponseEntity.status(HttpStatus.OK).body(video);
        }
        CustomMessage customMessage = new CustomMessage();
        customMessage.setMessage("Something went Wrong !!");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(customMessage);
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.getVideoById(videoId);

        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity
                .ok().contentType(MediaType.parseMediaType(contentType))
                .body(resource);

    }

    @GetMapping("stream/range/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId,
                                           @RequestHeader(value = "Range", required = false) String range) {

        System.out.println(range);

        Video video = videoService.getVideoById(videoId);
        Path path = Paths.get(video.getFilePath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();

        if(range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }
        long rangeStart, rangeEnd;

        String[] ranges = range.replace("bytes=","").split("-");
        rangeStart = Long.parseLong(ranges[0]);
        rangeEnd = rangeStart + CHUNK_SIZE - 1;

        if(rangeEnd>fileLength) {
            rangeEnd = fileLength-1;
        }

        System.out.println("range start : " + rangeStart);
        System.out.println("range End : " + rangeEnd);

        InputStream inputStream;

        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int)contentLength];
            int read = inputStream.read(data, 0, data.length);
            System.out.println("read(no. of bytes) : "+ read);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Range", "bytes "+ rangeStart + "-" + rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires","0");
            httpHeaders.add("X-Content-Type-Options", "nosniff");
            httpHeaders.setContentLength(contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(httpHeaders)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Value("${file.video.hls}")
    private String HLS_DIR;

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HLS_DIR, videoId, "master.m3u8");

        if(!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                .body(resource);
    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(@PathVariable String videoId, @PathVariable String segment) {
        Path path = Paths.get(HLS_DIR, videoId, segment + ".ts");

        if(!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }


}
