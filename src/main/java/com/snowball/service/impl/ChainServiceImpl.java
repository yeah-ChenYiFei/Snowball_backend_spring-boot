package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.dto.SegmentCommentCreateDTO;
import com.snowball.entity.ChainSegment;
import com.snowball.entity.SegmentComment;
import com.snowball.entity.StoryChain;
import com.snowball.repository.ChainSegmentRepository;
import com.snowball.repository.SegmentCommentRepository;
import com.snowball.repository.StoryChainRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.ChainService;
import com.snowball.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChainServiceImpl implements ChainService {

    private final StoryChainRepository chainRepository;
    private final ChainSegmentRepository segmentRepository;
    private final SegmentCommentRepository commentRepository;
    private final UserRepository userRepository;

    public ChainServiceImpl(StoryChainRepository chainRepository, ChainSegmentRepository segmentRepository,
                            SegmentCommentRepository commentRepository, UserRepository userRepository) {
        this.chainRepository = chainRepository;
        this.segmentRepository = segmentRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    private ChainVO toChainVO(StoryChain chain) {
        ChainVO vo = new ChainVO();
        vo.setId(chain.getId());
        vo.setCreatorId(chain.getCreatorId());
        vo.setTitle(chain.getTitle());
        vo.setDescription(chain.getDescription());
        vo.setStatus(chain.getStatus().name());
        vo.setGroupId(chain.getGroupId());
        vo.setDeadline(chain.getDeadline());
        vo.setCreatedAt(chain.getCreatedAt());
        userRepository.findById(chain.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        List<ChainSegment> segments = segmentRepository.findByChainIdOrderByCreatedAtAsc(chain.getId());
        vo.setSegmentCount(segments.size());
        if (!segments.isEmpty()) {
            String body = segments.get(0).getBody();
            vo.setFirstSegmentBody(body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body);
        }
        return vo;
    }

    @Override
    public List<ChainVO> getAllChains() {
        return chainRepository.findAll().stream().map(this::toChainVO).toList();
    }

    @Override
    public List<ChainVO> getPublicChains() {
        return chainRepository.findByGroupIdIsNullOrderByCreatedAtDesc().stream().map(this::toChainVO).toList();
    }

    @Override
    public List<ChainVO> getGroupChains(Long groupId) {
        return chainRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream().map(this::toChainVO).toList();
    }

    @Override
    public ChainDetailVO getChainDetail(Long chainId) {
        StoryChain chain = chainRepository.findById(chainId)
                .orElseThrow(() -> new BusinessException(404, "接龙不存在"));

        ChainDetailVO vo = new ChainDetailVO();
        vo.setId(chain.getId());
        vo.setCreatorId(chain.getCreatorId());
        vo.setTitle(chain.getTitle());
        vo.setDescription(chain.getDescription());
        vo.setStatus(chain.getStatus().name());
        vo.setGroupId(chain.getGroupId());
        vo.setDeadline(chain.getDeadline());
        vo.setCreatedAt(chain.getCreatedAt());
        userRepository.findById(chain.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));

        List<ChainSegment> segments = segmentRepository.findByChainIdOrderByCreatedAtAsc(chainId);
        List<ChainSegmentVO> segmentVOList = new ArrayList<>();
        for (ChainSegment seg : segments) {
            ChainSegmentVO segVO = new ChainSegmentVO();
            segVO.setId(seg.getId());
            segVO.setUserId(seg.getUserId());
            segVO.setBody(seg.getBody());
            segVO.setStatus(seg.getStatus().name());
            segVO.setPrevSegmentId(seg.getPrevSegmentId());
            segVO.setDepth(seg.getDepth());
            segVO.setCommentCount(commentRepository.countBySegmentId(seg.getId()));
            segVO.setIsAiGenerated(seg.getIsAiGenerated());
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
        chain.setDescription(dto.getDescription());
        chain.setDeadline(dto.getDeadline());
        chain.setGroupId(dto.getGroupId());
        chainRepository.save(chain);

        // First segment is auto-approved (initiator's own entry)
        ChainSegment seg = new ChainSegment();
        seg.setChainId(chain.getId());
        seg.setUserId(userId);
        seg.setBody(dto.getFirstSegmentBody());
        seg.setStatus(ChainSegment.SegmentStatus.APPROVED);
        segmentRepository.save(seg);

        return toChainVO(chain);
    }

    @Override
    @Transactional
    public ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto) {
        StoryChain chain = chainRepository.findById(chainId)
                .orElseThrow(() -> new BusinessException(404, "接龙不存在"));

        ChainSegment seg = new ChainSegment();
        seg.setChainId(chainId);
        seg.setUserId(userId);
        seg.setBody(dto.getBody());
        seg.setPrevSegmentId(dto.getPrevSegmentId());
        seg.setDepth(dto.getPrevSegmentId() != null ? 2 : 1);
        seg.setIsAiGenerated(dto.getIsAi() != null && dto.getIsAi());
        // Joiner segments go to PENDING for initiator review
        boolean isInitiator = chain.getCreatorId().equals(userId);
        seg.setStatus(isInitiator ? ChainSegment.SegmentStatus.APPROVED : ChainSegment.SegmentStatus.PENDING);
        seg = segmentRepository.save(seg);

        ChainSegmentVO vo = new ChainSegmentVO();
        vo.setId(seg.getId());
        vo.setUserId(seg.getUserId());
        vo.setBody(seg.getBody());
        vo.setStatus(seg.getStatus().name());
        vo.setPrevSegmentId(seg.getPrevSegmentId());
        vo.setDepth(seg.getDepth());
        vo.setCommentCount(0);
        vo.setIsAiGenerated(seg.getIsAiGenerated());
        vo.setCreatedAt(seg.getCreatedAt());
        userRepository.findById(userId).ifPresent(user -> vo.setUsername(user.getUsername()));
        return vo;
    }

    @Override
    public List<SegmentCommentVO> getComments(Long segmentId) {
        return commentRepository.findBySegmentIdOrderByCreatedAtAsc(segmentId).stream().map(c -> {
            SegmentCommentVO vo = new SegmentCommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setBody(c.getBody());
            vo.setCreatedAt(c.getCreatedAt());
            userRepository.findById(c.getUserId()).ifPresent(user -> vo.setUsername(user.getUsername()));
            return vo;
        }).toList();
    }

    @Override
    public SegmentCommentVO addComment(Long segmentId, Long userId, SegmentCommentCreateDTO dto) {
        SegmentComment comment = new SegmentComment();
        comment.setSegmentId(segmentId);
        comment.setUserId(userId);
        comment.setBody(dto.getBody());
        comment = commentRepository.save(comment);

        SegmentCommentVO vo = new SegmentCommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setBody(comment.getBody());
        vo.setCreatedAt(comment.getCreatedAt());
        userRepository.findById(userId).ifPresent(user -> vo.setUsername(user.getUsername()));
        return vo;
    }

    @Override
    @Transactional
    public void reviewSegment(Long segmentId, Long reviewerUserId, String status) {
        ChainSegment seg = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new BusinessException(404, "段落不存在"));

        StoryChain chain = chainRepository.findById(seg.getChainId())
                .orElseThrow(() -> new BusinessException(404, "接龙不存在"));

        if (!chain.getCreatorId().equals(reviewerUserId)) {
            throw new BusinessException(403, "只有发起人可以审核");
        }

        seg.setStatus(ChainSegment.SegmentStatus.valueOf(status));
        segmentRepository.save(seg);
    }

    @Override
    public void deleteSegment(Long segmentId, Long userId) {
        ChainSegment seg = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new BusinessException(404, "段落不存在"));

        StoryChain chain = chainRepository.findById(seg.getChainId())
                .orElseThrow(() -> new BusinessException(404, "接龙不存在"));

        // Author or chain initiator can delete
        if (!seg.getUserId().equals(userId) && !chain.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权删除此段落");
        }

        segmentRepository.delete(seg);
    }

    @Override
    public List<ChainVO> getUserChainActivities(Long userId) {
        List<Long> chainIds = segmentRepository.findDistinctChainIdsByUserId(userId);
        return chainRepository.findAllById(chainIds).stream()
                .filter(c -> c.getGroupId() == null) // only public chains
                .map(this::toChainVO)
                .toList();
    }
}
