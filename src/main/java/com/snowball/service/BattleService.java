package com.snowball.service;

import com.snowball.dto.BattleCreateDTO;
import com.snowball.dto.BattleEntryDTO;
import com.snowball.dto.BattleReviewDTO;
import com.snowball.vo.BattleEntryVO;
import com.snowball.vo.BattleVO;

import java.util.List;

public interface BattleService {
    BattleVO createBattle(Long groupId, Long userId, BattleCreateDTO dto);
    List<BattleVO> getGroupBattles(Long groupId);
    BattleVO getBattleDetail(Long battleId);
    BattleEntryVO submitEntry(Long battleId, Long userId, BattleEntryDTO dto);
    void addReview(Long entryId, Long reviewerId, BattleReviewDTO dto);
    void closeBattle(Long battleId, Long userId);
}
