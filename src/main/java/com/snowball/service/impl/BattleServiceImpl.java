package com.snowball.service.impl;

import com.snowball.dto.BattleCreateDTO;
import com.snowball.dto.BattleEntryDTO;
import com.snowball.dto.BattleReviewDTO;
import com.snowball.entity.BattleEntry;
import com.snowball.entity.BattleReview;
import com.snowball.entity.WritingBattle;
import com.snowball.repository.BattleEntryRepository;
import com.snowball.repository.BattleReviewRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WritingBattleRepository;
import com.snowball.service.BattleService;
import com.snowball.vo.BattleEntryVO;
import com.snowball.vo.BattleReviewVO;
import com.snowball.vo.BattleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BattleServiceImpl implements BattleService {

    private final WritingBattleRepository battleRepository;
    private final BattleEntryRepository entryRepository;
    private final BattleReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public BattleServiceImpl(WritingBattleRepository battleRepository,
                             BattleEntryRepository entryRepository,
                             BattleReviewRepository reviewRepository,
                             UserRepository userRepository) {
        this.battleRepository = battleRepository;
        this.entryRepository = entryRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BattleVO createBattle(Long groupId, Long userId, BattleCreateDTO dto) {
        WritingBattle battle = new WritingBattle();
        battle.setGroupId(groupId);
        battle.setCreatorId(userId);
        battle.setTopic(dto.getTopic());
        battle.setDescription(dto.getDescription());
        battle.setDeadline(dto.getDeadline());
        battle.setStatus("OPEN");
        if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
            battle.setParticipantIds(dto.getParticipantIds().stream()
                    .map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        }
        battleRepository.save(battle);

        return toVO(battle);
    }

    @Override
    public List<BattleVO> getGroupBattles(Long groupId) {
        return battleRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream().map(this::toVO).toList();
    }

    @Override
    public BattleVO getBattleDetail(Long battleId) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new RuntimeException("擂台不存在"));
        BattleVO vo = toVO(battle);
        List<BattleEntry> entries = entryRepository.findByBattleIdOrderByCreatedAtAsc(battleId);
        List<BattleEntryVO> entryVOs = new ArrayList<>();
        for (BattleEntry e : entries) {
            entryVOs.add(toEntryVO(e));
        }
        vo.setEntries(entryVOs);
        return vo;
    }

    @Override
    public BattleEntryVO submitEntry(Long battleId, Long userId, BattleEntryDTO dto) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new RuntimeException("擂台不存在"));
        if (!"OPEN".equals(battle.getStatus())) {
            throw new RuntimeException("擂台已关闭，无法提交");
        }
        // Check participant permission
        if (battle.getParticipantIds() != null && !battle.getParticipantIds().isBlank()) {
            boolean isParticipant = java.util.Arrays.stream(battle.getParticipantIds().split(","))
                    .map(String::trim).anyMatch(id -> id.equals(String.valueOf(userId)));
            if (!isParticipant) {
                throw new RuntimeException("你未被邀请参加此擂台");
            }
        }
        BattleEntry entry = new BattleEntry();
        entry.setBattleId(battleId);
        entry.setUserId(userId);
        entry.setTitle(dto.getTitle());
        entry.setBody(dto.getBody());
        entryRepository.save(entry);

        return toEntryVO(entry);
    }

    @Override
    public void addReview(Long entryId, Long reviewerId, BattleReviewDTO dto) {
        BattleEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("参赛作品不存在"));
        WritingBattle battle = battleRepository.findById(entry.getBattleId())
                .orElseThrow(() -> new RuntimeException("擂台不存在"));
        if (!"VOTING".equals(battle.getStatus()) && !"CLOSED".equals(battle.getStatus())) {
            throw new RuntimeException("当前阶段不能评审");
        }
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 10) {
            throw new RuntimeException("评分需在1-10之间");
        }
        BattleReview review = new BattleReview();
        review.setEntryId(entryId);
        review.setReviewerId(reviewerId);
        review.setScore(dto.getScore());
        review.setComment(dto.getComment());
        reviewRepository.save(review);

        // Update avg score
        List<BattleReview> allReviews = reviewRepository.findByEntryIdOrderByCreatedAtAsc(entryId);
        double avg = allReviews.stream().mapToInt(BattleReview::getScore).average().orElse(0);
        entry.setAvgScore(Math.round(avg * 10.0) / 10.0);
        entry.setVoteCount(allReviews.size());
        entryRepository.save(entry);
    }

    @Override
    public void closeBattle(Long battleId, Long userId) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new RuntimeException("擂台不存在"));
        if (!battle.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有发起人可以关闭擂台");
        }
        battle.setStatus("VOTING");
        battleRepository.save(battle);
    }

    private BattleVO toVO(WritingBattle b) {
        BattleVO vo = new BattleVO();
        vo.setId(b.getId());
        vo.setGroupId(b.getGroupId());
        vo.setCreatorId(b.getCreatorId());
        vo.setTopic(b.getTopic());
        vo.setDescription(b.getDescription());
        vo.setDeadline(b.getDeadline());
        vo.setStatus(b.getStatus());
        vo.setCreatedAt(b.getCreatedAt());
        userRepository.findById(b.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        return vo;
    }

    private BattleEntryVO toEntryVO(BattleEntry e) {
        BattleEntryVO vo = new BattleEntryVO();
        vo.setId(e.getId());
        vo.setBattleId(e.getBattleId());
        vo.setUserId(e.getUserId());
        vo.setTitle(e.getTitle());
        vo.setBody(e.getBody());
        vo.setAvgScore(e.getAvgScore());
        vo.setVoteCount(e.getVoteCount());
        vo.setCreatedAt(e.getCreatedAt());
        userRepository.findById(e.getUserId()).ifPresent(user -> vo.setUsername(user.getUsername()));

        List<BattleReview> reviews = reviewRepository.findByEntryIdOrderByCreatedAtAsc(e.getId());
        List<BattleReviewVO> reviewVOs = new ArrayList<>();
        for (BattleReview r : reviews) {
            BattleReviewVO rvo = new BattleReviewVO();
            rvo.setId(r.getId());
            rvo.setEntryId(r.getEntryId());
            rvo.setReviewerId(r.getReviewerId());
            rvo.setScore(r.getScore());
            rvo.setComment(r.getComment());
            rvo.setCreatedAt(r.getCreatedAt());
            userRepository.findById(r.getReviewerId()).ifPresent(user -> rvo.setReviewerName(user.getUsername()));
            reviewVOs.add(rvo);
        }
        vo.setReviews(reviewVOs);
        return vo;
    }
}
