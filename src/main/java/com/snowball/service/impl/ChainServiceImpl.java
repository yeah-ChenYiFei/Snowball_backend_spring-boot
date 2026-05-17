//src/service/impl/ChainServiceImpl.java
package com.snowball.service.impl;

import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.entity.ChainSegment;
import com.snowball.entity.StoryChain;
import com.snowball.repository.ChainSegmentRepository;
import com.snowball.repository.StoryChainRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.ChainService;
import com.snowball.vo.ChainDetailVO;
import com.snowball.vo.ChainSegmentVO;
import com.snowball.vo.ChainVO;
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

            userRepository.findById(chain.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));

            List<ChainSegment> segments = segmentRepository.findByChainIdOrderByCreatedAtAsc(chain.getId());
            if (!segments.isEmpty()) {
                String body = segments.get(0).getBody();
                vo.setFirstSegmentBody(body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body);
            }
            return vo;
        }).toList();
    }

    @Override
    public ChainDetailVO getChainDetail(Long chainId) {
        StoryChain chain = chainRepository.findById(chainId)
                .orElseThrow(() -> new RuntimeException("接龙不存在"));

        ChainDetailVO vo = new ChainDetailVO();
        vo.setId(chain.getId());
        vo.setCreatorId(chain.getCreatorId());
        vo.setTitle(chain.getTitle());
        vo.setStatus(chain.getStatus().name());
        vo.setCreatedAt(chain.getCreatedAt());

        userRepository.findById(chain.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));

        List<ChainSegment> segments = segmentRepository.findByChainIdOrderByCreatedAtAsc(chainId);
        List<ChainSegmentVO> segmentVOList = new ArrayList<>();
        for (ChainSegment seg : segments) {
            ChainSegmentVO segVO = new ChainSegmentVO();
            segVO.setId(seg.getId());
            segVO.setUserId(seg.getUserId());
            segVO.setBody(seg.getBody());
            segVO.setPrevSegmentId(seg.getPrevSegmentId());
            segVO.setDepth(seg.getDepth());
            segVO.setCreatedAt(seg.getCreatedAt());
            userRepository.findById(seg.getUserId()).ifPresent(user -> segVO.setUsername(user.getUsername()));
            segmentVOList.add(segVO);
        }
        vo.setSegments(segmentVOList);
        return vo;
    }

    @Override
    @Transactional
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
        vo.setCreatorId(chain.getCreatorId());
        vo.setTitle(chain.getTitle());
        vo.setStatus(chain.getStatus().name());
        vo.setCreatedAt(chain.getCreatedAt());

        userRepository.findById(userId).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        String body = dto.getFirstSegmentBody();
        vo.setFirstSegmentBody(body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body);

        return vo;
    }

    @Override
    public ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto) {
        ChainSegment seg = new ChainSegment();
        seg.setChainId(chainId);
        seg.setUserId(userId);
        seg.setBody(dto.getBody());

        if (dto.getPrevSegmentId() != null) {
            seg.setPrevSegmentId(dto.getPrevSegmentId());
            seg.setDepth(2);
        }
        seg = segmentRepository.save(seg);

        ChainSegmentVO vo = new ChainSegmentVO();
        vo.setId(seg.getId());
        vo.setUserId(seg.getUserId());
        vo.setBody(seg.getBody());
        vo.setPrevSegmentId(seg.getPrevSegmentId());
        vo.setDepth(seg.getDepth());
        vo.setCreatedAt(seg.getCreatedAt());
        userRepository.findById(userId).ifPresent(user -> vo.setUsername(user.getUsername()));
        return vo;
    }
}
