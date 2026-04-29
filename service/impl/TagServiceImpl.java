package com.example.snowball.service.impl;

import com.example.snowball.common.BusinessException;
import com.example.snowball.dto.TagCreateDTO;
import com.example.snowball.entity.Tag;
import com.example.snowball.repository.TagRepository;
import com.example.snowball.service.TagService;
import com.example.snowball.vo.TagVO;
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
