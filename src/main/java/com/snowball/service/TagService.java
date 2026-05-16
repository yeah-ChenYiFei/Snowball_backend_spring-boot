package com.snowball.service;

import com.snowball.dto.TagCreateDTO;
import com.snowball.vo.TagVO;
import java.util.List;

public interface TagService {
    List<TagVO> getAllTags();
    TagVO createTag(TagCreateDTO dto);
}
