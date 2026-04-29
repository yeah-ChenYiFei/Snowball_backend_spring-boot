//src/service/impl/ChainServiceImpl.java
package com.example.snowball.service.impl;

import com.example.snowball.dto.ChainCreateDTO;
import com.example.snowball.dto.ChainSegmentCreateDTO;
import com.example.snowball.entity.ChainSegment;
import com.example.snowball.entity.StoryChain;
import com.example.snowball.repository.ChainSegmentRepository;
import com.example.snowball.repository.StoryChainRepository;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.service.ChainService;
import com.example.snowball.vo.ChainSegmentVO;
import com.example.snowball.vo.ChainVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChainServiceImpl implements ChainService {

    private final StoryChainRepository chainRepository;
    private final ChainSegmentRepository segmentRepository;
    private final UserRepository userRepository;

    public ChainServiceImpl(StoryChainRepository chainRepository, ChainSegmentRepository segmentRepository, UserRepository userRepository) {
        this.chainRepository = chainRepository;
        this.segmentRepository = segmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ChainVO> getAllChains() {
        return chainRepository.findAll().stream().map(chain -> {
            ChainVO vo = new ChainVO();
            vo.setId(chain.getId());
            vo.setCreatorId(chain.getCreatorId());
            vo.setTitle(chain.getTitle());
            vo.setStatus(chain.getStatus().name());
            vo.setCreatedAt(chain.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public List<ChainSegmentVO> getChainDetail(Long chainId) {
        List<ChainSegment> segments = segmentRepository.findByChainIdOrderByCreatedAtAsc(chainId);
        List<ChainSegmentVO> voList = new ArrayList<>();
        for (ChainSegment seg : segments) {
            ChainSegmentVO vo = new ChainSegmentVO();
            vo.setId(seg.getId());
            vo.setUserId(seg.getUserId());
            vo.setBody(seg.getBody());
            vo.setPrevSegmentId(seg.getPrevSegmentId());
            vo.setDepth(seg.getDepth());
            vo.setCreatedAt(seg.getCreatedAt());
            // ✅ 补全作者名
            userRepository.findById(seg.getUserId()).ifPresent(user -> vo.setUsername(user.getUsername()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional // ✅ 存链 + 存首段，加事务
    public ChainVO createChain(Long userId, ChainCreateDTO dto) {
        StoryChain chain = new StoryChain();
        chain.setCreatorId(userId);
        chain.setTitle(dto.getTitle());
        chainRepository.save(chain);

        ChainSegment seg = new ChainSegment();
        seg.setChainId(chain.getId());
        seg.setUserId(userId);
        seg.setBody(dto.getFirstSegmentBody());
        segmentRepository.save(seg);

        ChainVO vo = new ChainVO();
        vo.setId(chain.getId());
        vo.setTitle(chain.getTitle());
        vo.setStatus(chain.getStatus().name());
        vo.setCreatedAt(chain.getCreatedAt());
        return vo;
    }

    @Override
    public ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto) {
        ChainSegment seg = new ChainSegment();
        seg.setChainId(chainId);
        seg.setUserId(userId);
        seg.setBody(dto.getBody());

        if(dto.getPrev_segment_id() != null && !dto.getPrev_segment_id().isEmpty()) {
            seg.setPrevSegmentId(Long.parseLong(dto.getPrev_segment_id()));
            seg.setDepth(2);
        }
        seg = segmentRepository.save(seg);

        ChainSegmentVO vo = new ChainSegmentVO();
        vo.setId(seg.getId());
        vo.setBody(seg.getBody());
        vo.setDepth(seg.getDepth());
        userRepository.findById(userId).ifPresent(user -> vo.setUsername(user.getUsername()));
        return vo;
    }
}
