package com.leyou.upload.web;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author YuTian
 * @date 2019/4/4 9:45
 */
@Controller
@RequestMapping("upload")
public class UpLoadController {

     @Autowired
    private UploadService uploadService;

      @PostMapping("image")
     public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){

        return ResponseEntity.ok(uploadService.uploadImage(file));
        }
}
