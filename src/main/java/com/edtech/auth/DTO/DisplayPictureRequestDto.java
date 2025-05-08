package com.edtech.auth.DTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DisplayPictureRequestDto {
    
    private MultipartFile displayPicture; 

}
