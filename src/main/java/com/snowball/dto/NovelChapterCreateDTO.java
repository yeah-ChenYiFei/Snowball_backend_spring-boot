package com.snowball.dto;

import lombok.Data;

@Data
public class NovelChapterCreateDTO {
    private String section = "main";
    private Integer volumeNumber = 0;
    private Integer chapterNumber = 1;
    private String title;
    private String body;
}
