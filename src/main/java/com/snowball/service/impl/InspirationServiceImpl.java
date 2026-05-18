package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.InspirationCreateDTO;
import com.snowball.dto.InspirationUpdateDTO;
import com.snowball.entity.Inspiration;
import com.snowball.repository.InspirationRepository;
import com.snowball.service.InspirationService;
import com.snowball.vo.InspirationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InspirationServiceImpl implements InspirationService {

    private final InspirationRepository inspirationRepository;

    public InspirationServiceImpl(InspirationRepository inspirationRepository) {
        this.inspirationRepository = inspirationRepository;
    }

    @Override
    public List<InspirationVO> getMyInspirations(Long userId) {
        return inspirationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public InspirationVO addInspiration(Long userId, InspirationCreateDTO dto) {
        Inspiration insp = new Inspiration();
        insp.setUserId(userId);
        insp.setContent(dto.getContent());
        insp = inspirationRepository.save(insp);
        return toVO(insp);
    }

    @Override
    @Transactional
    public InspirationVO updateInspiration(Long id, Long userId, InspirationUpdateDTO dto) {
        Inspiration insp = inspirationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "灵感记录不存在"));
        if (!insp.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人灵感");
        }
        insp.setContent(dto.getContent());
        insp = inspirationRepository.save(insp);
        return toVO(insp);
    }

    @Override
    @Transactional
    public void deleteInspiration(Long id, Long userId) {
        Inspiration insp = inspirationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "灵感记录不存在"));
        if (!insp.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人灵感");
        }
        inspirationRepository.delete(insp);
    }

    private InspirationVO toVO(Inspiration insp) {
        InspirationVO vo = new InspirationVO();
        vo.setId(insp.getId());
        vo.setUserId(insp.getUserId());
        vo.setContent(insp.getContent());
        vo.setCreatedAt(insp.getCreatedAt());
        vo.setUpdatedAt(insp.getUpdatedAt());
        return vo;
    }
}
