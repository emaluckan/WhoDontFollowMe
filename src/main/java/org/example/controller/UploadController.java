package org.example.controller;

import org.example.service.InstagramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class UploadController {

    private final InstagramService instagramService;

    public UploadController(InstagramService instagramService) {
        this.instagramService = instagramService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadZip(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            return ResponseEntity.ok(
                    instagramService.procesarZip(file)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }
}