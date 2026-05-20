package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.BattleCreateDTO;
import com.snowball.dto.BattleEntryDTO;
import com.snowball.dto.BattleReviewDTO;
import com.snowball.entity.BattleEntry;
import com.snowball.entity.BattleReview;
import com.snowball.entity.User;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        battle.setStatus(WritingBattle.BattleStatus.OPEN);
        if (dto.getParticipantIds() != null && !dto.getParticipantIds().isEmpty()) {
            battle.setParticipantIds(dto.getParticipantIds().stream()
                    .map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        }
        battleRepository.save(battle);

        Map<Long, String> usernameMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return toVO(battle, usernameMap);
    }

    @Override
    public List<BattleVO> getGroupBattles(Long groupId) {
        List<WritingBattle> battles = battleRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        if (battles.isEmpty()) return List.of();

        List<Long> creatorIds = battles.stream().map(WritingBattle::getCreatorId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return battles.stream().map(b -> toVO(b, usernameMap)).toList();
    }

    @Override
    public BattleVO getBattleDetail(Long battleId) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new BusinessException(404, "擂台不存在"));

        Map<Long, String> usernameMap = userRepository.findAllById(List.of(battle.getCreatorId())).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        BattleVO vo = toVO(battle, usernameMap);

        List<BattleEntry> entries = entryRepository.findByBattleIdOrderByCreatedAtAsc(battleId);
        if (!entries.isEmpty()) {
            vo.setEntries(batchToEntryVOList(entries));
        } else {
            vo.setEntries(List.of());
        }
        return vo;
    }

    @Override
    public BattleEntryVO submitEntry(Long battleId, Long userId, BattleEntryDTO dto) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new BusinessException(404, "擂台不存在"));
        if (battle.getStatus() != WritingBattle.BattleStatus.OPEN) {
            throw new BusinessException(400, "擂台已关闭，无法提交");
        }
        if (battle.getParticipantIds() != null && !battle.getParticipantIds().isBlank()) {
            boolean isParticipant = java.util.Arrays.stream(battle.getParticipantIds().split(","))
                    .map(String::trim).anyMatch(id -> id.equals(String.valueOf(userId)));
            if (!isParticipant) {
                throw new BusinessException(403, "你未被邀请参加此擂台");
            }
        }
        BattleEntry entry = new BattleEntry();
        entry.setBattleId(battleId);
        entry.setUserId(userId);
        entry.setTitle(dto.getTitle());
        entry.setBody(dto.getBody());
        entryRepository.save(entry);

        Map<Long, String> usernameMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return toEntryVO(entry, usernameMap, Map.of());
    }

    @Override
    public void addReview(Long entryId, Long reviewerId, BattleReviewDTO dto) {
        BattleEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException(404, "参赛作品不存在"));
        WritingBattle battle = battleRepository.findById(entry.getBattleId())
                .orElseThrow(() -> new BusinessException(404, "擂台不存在"));
        if (battle.getStatus() != WritingBattle.BattleStatus.VOTING && battle.getStatus() != WritingBattle.BattleStatus.CLOSED) {
            throw new BusinessException(400, "当前阶段不能评审");
        }
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 10) {
            throw new BusinessException(400, "评分需在1-10之间");
        }
        BattleReview review = new BattleReview();
        review.setEntryId(entryId);
        review.setReviewerId(reviewerId);
        review.setScore(dto.getScore());
        review.setComment(dto.getComment());
        reviewRepository.save(review);

        List<BattleReview> allReviews = reviewRepository.findByEntryIdOrderByCreatedAtAsc(entryId);
        double avg = allReviews.stream().mapToInt(BattleReview::getScore).average().orElse(0);
        entry.setAvgScore(Math.round(avg * 10.0) / 10.0);
        entry.setVoteCount(allReviews.size());
        entryRepository.save(entry);
    }

    @Override
    public void closeBattle(Long battleId, Long userId) {
        WritingBattle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new BusinessException(404, "擂台不存在"));
        if (!battle.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有发起人可以关闭擂台");
        }
        battle.setStatus(WritingBattle.BattleStatus.VOTING);
        battleRepository.save(battle);
    }

    private BattleVO toVO(WritingBattle b, Map<Long, String> usernameMap) {
        BattleVO vo = new BattleVO();
        vo.setId(b.getId());
        vo.setGroupId(b.getGroupId());
        vo.setCreatorId(b.getCreatorId());
        vo.setTopic(b.getTopic());
        vo.setDescription(b.getDescription());
        vo.setDeadline(b.getDeadline());
        vo.setStatus(b.getStatus().name());
        vo.setCreatedAt(b.getCreatedAt());
        vo.setCreatorName(usernameMap.getOrDefault(b.getCreatorId(), ""));
        return vo;
    }

    private List<BattleEntryVO> batchToEntryVOList(List<BattleEntry> entries) {
        // Collect all entry user IDs and fetch usernames
        List<Long> entryUserIds = entries.stream().map(BattleEntry::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(entryUserIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        // Batch fetch all reviews for all entries
        List<Long> entryIds = entries.stream().map(BattleEntry::getId).toList();
        List<BattleReview> allReviews = reviewRepository.findByEntryIdInOrderByCreatedAtAsc(entryIds);

        // Group reviews by entryId
        Map<Long, List<BattleReview>> reviewsByEntry = allReviews.stream()
                .collect(Collectors.groupingBy(BattleReview::getEntryId));

        // Collect all reviewer IDs and batch fetch their usernames
        List<Long> reviewerIds = allReviews.stream().map(BattleReview::getReviewerId).distinct().toList();
        Map<Long, String> reviewerNameMap = userRepository.findAllById(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return entries.stream()
                .map(e -> toEntryVO(e, usernameMap, reviewsByEntry, reviewerNameMap))
                .toList();
    }

    private BattleEntryVO toEntryVO(BattleEntry e, Map<Long, String> usernameMap,
                                    Map<Long, List<BattleReview>> reviewsByEntry,
                                    Map<Long, String> reviewerNameMap) {
        BattleEntryVO vo = new BattleEntryVO();
        vo.setId(e.getId());
        vo.setBattleId(e.getBattleId());
        vo.setUserId(e.getUserId());
        vo.setTitle(e.getTitle());
        vo.setBody(e.getBody());
        vo.setAvgScore(e.getAvgScore());
        vo.setVoteCount(e.getVoteCount());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUsername(usernameMap.getOrDefault(e.getUserId(), ""));

        List<BattleReview> reviews = reviewsByEntry.getOrDefault(e.getId(), List.of());
        List<BattleReviewVO> reviewVOs = new ArrayList<>();
        for (BattleReview r : reviews) {
            BattleReviewVO rvo = new BattleReviewVO();
            rvo.setId(r.getId());
            rvo.setEntryId(r.getEntryId());
            rvo.setReviewerId(r.getReviewerId());
            rvo.setScore(r.getScore());
            rvo.setComment(r.getComment());
            rvo.setCreatedAt(r.getCreatedAt());
            rvo.setReviewerName(reviewerNameMap.getOrDefault(r.getReviewerId(), ""));
            reviewVOs.add(rvo);
        }
        vo.setReviews(reviewVOs);
        return vo;
    }

    /** Used for single-entry cases (submitEntry) where we don't have preloaded maps */
    private BattleEntryVO toEntryVO(BattleEntry e, Map<Long, String> usernameMap,
                                    Map<Long, List<BattleReview>> reviewsByEntry) {
        Map<Long, String> reviewerNameMap = Map.of();
        if (reviewsByEntry.containsKey(e.getId())) {
            List<Long> rIds = reviewsByEntry.get(e.getId()).stream()
                    .map(BattleReview::getReviewerId).distinct().toList();
            if (!rIds.isEmpty()) {
                reviewerNameMap = userRepository.findAllById(rIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername));
            }
        }
        return toEntryVO(e, usernameMap, reviewsByEntry, reviewerNameMap);
    }
}
