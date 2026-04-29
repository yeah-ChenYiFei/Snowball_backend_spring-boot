package com.example.snowball.service;

import com.example.snowball.dto.TagCreateDTO;
import com.example.snowball.vo.TagVO;
import java.util.List;

public interface TagService {
    List<TagVO> getAllTags();
    TagVO createTag(TagCreateDTO dto);
}
