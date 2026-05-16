package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.TagCreateDTO;
import com.snowball.entity.Tag;
import com.snowball.repository.TagRepository;
import com.snowball.service.TagService;
import com.snowball.vo.TagVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<TagVO> getAllTags() {
        return tagRepository.findAll().stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setDescription(tag.getDescription());
            return vo;
        }).toList();
    }

    @Override
    public TagVO createTag(TagCreateDTO dto) {
        if(tagRepository.findByName(dto.getName()).isPresent()) {
            throw new BusinessException(400, "标签已存在");
        }
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        tag = tagRepository.save(tag);

        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setDescription(tag.getDescription());
        return vo;
    }
}
