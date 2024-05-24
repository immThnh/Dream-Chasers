package com.example.demo.service;

import com.example.demo.cloudinary.CloudService;
import com.example.demo.dto.ResponseObject;
import com.example.demo.dto.SectionDTO;
import com.example.demo.dto.LessonDTO;
import com.example.demo.entity.data.Lesson;
import com.example.demo.entity.data.Section;
import com.example.demo.repository.data.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CloudService cloudService;

    public ResponseObject getById(int id) {
        var lesson = lessonRepository.findById(id).orElse(null);
        if(lesson == null) {
            return ResponseObject.builder().content("Lesson is not exist!").status(HttpStatus.BAD_REQUEST).build();
        }
        return  ResponseObject.builder().status(HttpStatus.OK).content(lesson).build();
    }

    public int updateLessonsOfSection(List<LessonDTO> newLessons, List<Lesson> currentLessons, Section section, List<String> videos, int indexVideo) {
        if(section.getLessons().size() == 0) {
            System.out.println("No lesson added to section");
            return indexVideo;
        }

        if(newLessons.isEmpty()) {
            for (var oldLesson:
                    currentLessons) {
                oldLesson.setSection(null);
                lessonRepository.delete(oldLesson);
            }
            return indexVideo;
        }

        var lessonsUpdate = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < newLessons.size(); i++) {
            LessonDTO newLesson = newLessons.get(i);
            Lesson currentLesson = currentLessons.stream()
                    .filter(l -> newLesson.getId() == l.getId())
                    .findFirst()
                    .orElse(null);
            if (currentLesson != null) {
                currentLesson.setDescription(newLesson.getDescription());
                currentLesson.setTitle(newLesson.getTitle());
                currentLesson.setLinkVideo(newLesson.getLinkVideo());
                indexVideo = updateVideo(newLesson, currentLesson, videos, indexVideo);
            } else {
                currentLesson = Lesson.builder()
                        .description(newLesson.getDescription())
                        .title(newLesson.getTitle())
                        .linkVideo(newLesson.getLinkVideo())
                        .section(section)
                        .build();
                section.getLessons().add(currentLesson);
                indexVideo = updateVideo(newLesson, currentLesson, videos, indexVideo);
            }
                lessonsUpdate.add(currentLesson);
        }
        for(int i = currentLessons.size() - 1; i >= 0; i--) {
            if(!lessonsUpdate.contains(currentLessons.get(i))) {
                lessonRepository.delete(currentLessons.get(i));
            }
        }
       return indexVideo;
    }

    public int updateVideo(LessonDTO lessonDTO, Lesson lesson, List<String> videos, int index) {
        if(videos != null && videos.size() > index) {
            switch (lessonDTO.getActionVideo()) {
                case "REMOVE" -> {
                    System.out.println("REMOVE  VIDEO");
                    lesson.setVideo(null);
                }
                case "UPDATE" -> {
                    System.out.println("UPDATE VIDEO");
                    lesson.setVideo(videos.get(index));
                    index++;
                }
                case "NONE" -> {
                    System.out.println("NONE  VIDEO");
                    lesson.setVideo(null);
                }
                case "KEEP" -> {
                    System.out.println("KEEP  VIDEO");
                    System.out.println(lessonDTO.getVideo());
                    lesson.setVideo(lessonDTO.getVideo());
                }
                default -> System.out.println("DEFAULT  VIDEO");
            }
        }
        return index;
    }

    public int addLessonForSection(List<LessonDTO> newLessons, Section section, SectionDTO sectionDTO, List<String> videos, int indexVideo) {
        if(section.getLessons() == null) {
            section.setLessons(new ArrayList<>());
        }
        for (LessonDTO lesson : sectionDTO.getLessons()
        ) {
            Lesson tLesson = Lesson.builder()
                    .section(section)
                    .date(LocalDateTime.now())
                    .title(lesson.getTitle())
                    .description(lesson.getDescription())
                    .linkVideo(lesson.getLinkVideo())
                    .build();
            section.getLessons().add(tLesson);
                if(videos != null && videos.size() > indexVideo)
                {
                    indexVideo = updateVideo(lesson, tLesson, videos, indexVideo);
                }
        }
        return indexVideo;
    }
 }
