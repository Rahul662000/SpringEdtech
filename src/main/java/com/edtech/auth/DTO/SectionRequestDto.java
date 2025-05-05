package com.edtech.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionRequestDto {

    private String sectionName;
    private Long courseId;
    
}
