package com.stream.app.stream_spring_backend.repositories;

import com.stream.app.stream_spring_backend.entites.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByVideoTitle(String title);
}
