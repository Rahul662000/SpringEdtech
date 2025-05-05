package com.edtech.auth.DTO;

import java.util.List;

import lombok.Data;

@Data
public class SectionDTO {
    private Long id;
    private String sectionName;
    private List<SubSectionDTO> subSections;
}
