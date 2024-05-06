package com.example.demo.controller.PrivateController;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.ResponseObject;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@RequestMapping("/api/v1/private/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<ResponseObject> create(@RequestPart CourseDTO course
            , @RequestPart(required = false) MultipartFile thumbnail
            , @RequestPart(required = false) MultipartFile courseVideo
            , @RequestPart(value = "videos", required = false) List<MultipartFile> videos ) {
        var result = courseService.addCourse(course, thumbnail, courseVideo, videos);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ResponseObject> updateCourse(@PathVariable int id, @RequestPart CourseDTO course
            , @RequestPart(required = false) MultipartFile thumbnail, @RequestPart(required = false) MultipartFile courseVideo, @RequestPart(value = "videos", required = false) List<MultipartFile> videos)  {

        var result = courseService.updateCourse(id, course, thumbnail, courseVideo, videos);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ResponseObject> restoreCourse(@PathVariable int id) {
        var result = courseService.restoreCourseById(id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
    @PutMapping("/delete/soft/{id}")
    public ResponseEntity<ResponseObject> softDelete(@PathVariable int id) {
        var result = courseService.softDelete(id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
    @DeleteMapping("/delete/hard/{id}")
    public ResponseEntity<ResponseObject> hardDelete(@PathVariable int id) {
        var result = courseService.hardDelete(id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/deleted/category")
    public ResponseEntity<ResponseObject> getCourseDeletedByCategoryId(@RequestParam("id") int id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        var result = courseService.getAllCourseDeletedByCategoryId(id, page, size);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/getAllDeleted")
    public ResponseEntity<ResponseObject> getAllDeleted(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "5") int size) {
        var result = courseService.getAllCourseDeleted(page, size);
        return ResponseEntity.status(result.getStatus()).body(result);
    }


}