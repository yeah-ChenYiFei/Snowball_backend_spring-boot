package com.snowball.controller;

import com.snowball.common.BusinessException;
import com.snowball.common.Result;
import jakarta.validation.Valid;
import com.snowball.dto.*;
import com.snowball.service.*;
import com.snowball.vo.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController extends BaseController {

    private final GroupService groupService;
    private final GroupMessageService messageService;
    private final ChainService chainService;
    private final BattleService battleService;

    public GroupController(GroupService groupService,
                           GroupMessageService messageService,
                           ChainService chainService,
                           BattleService battleService) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.chainService = chainService;
        this.battleService = battleService;
    }

    // ===== Group CRUD =====

    @GetMapping
    public Result<List<GroupVO>> getMyGroups() {
        return Result.success(groupService.getMyGroups(getCurrentUserId()));
    }

    @GetMapping("/search")
    public Result<List<GroupVO>> searchGroups(@RequestParam String q) {
        return Result.success(groupService.searchGroups(q));
    }

    @GetMapping("/{groupId}")
    public Result<GroupDetailVO> getGroupDetail(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupDetail(groupId));
    }

    @PostMapping
    public Result<GroupVO> createGroup(@Valid @RequestBody GroupCreateDTO dto) {
        return Result.success(groupService.createGroup(getCurrentUserId(), dto));
    }

    @PostMapping("/{groupId}/join")
    public Result<String> joinGroup(@PathVariable Long groupId) {
        groupService.joinGroup(groupId, getCurrentUserId());
        return Result.success("加入成功");
    }

    @PostMapping("/{groupId}/leave")
    public Result<String> leaveGroup(@PathVariable Long groupId) {
        groupService.leaveGroup(groupId, getCurrentUserId());
        return Result.success("退出成功");
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<String> kickMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.kickMember(groupId, getCurrentUserId(), userId);
        return Result.success("踢出成功");
    }

    @DeleteMapping("/{groupId}")
    public Result<String> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId, getCurrentUserId());
        return Result.success("群组已解散");
    }

    // ===== Members =====

    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getMembers(@PathVariable Long groupId) {
        return Result.success(groupService.getMembers(groupId));
    }

    // ===== Messages =====

    @GetMapping("/{groupId}/messages")
    public Result<List<GroupMessageVO>> getMessages(
            @PathVariable Long groupId,
            @RequestParam(required = false) Long since) {
        return Result.success(messageService.getMessages(groupId, since));
    }

    @PostMapping("/{groupId}/messages")
    public Result<GroupMessageVO> sendMessage(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMessageCreateDTO dto) {
        return Result.success(messageService.sendMessage(groupId, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{groupId}/messages/{msgId}")
    public Result<String> deleteMessage(
            @PathVariable Long groupId,
            @PathVariable Long msgId) {
        boolean isAdmin = isGroupAdmin(groupId);
        messageService.deleteMessage(groupId, msgId, getCurrentUserId(), isAdmin);
        return Result.success("消息已删除");
    }

    // ===== Group Chains =====

    @PostMapping("/{groupId}/chains")
    public Result<ChainVO> createGroupChain(
            @PathVariable Long groupId,
            @Valid @RequestBody ChainCreateDTO dto) {
        dto.setGroupId(groupId);
        ChainVO chain = chainService.createChain(getCurrentUserId(), dto);
        // Auto-send group message
        GroupMessageCreateDTO msg = new GroupMessageCreateDTO();
        msg.setBody(dto.getTitle());
        msg.setType("CHAIN_START");
        msg.setRefId(chain.getId());
        msg.setRefType("CHAIN");
        messageService.sendMessage(groupId, getCurrentUserId(), msg);
        return Result.success(chain);
    }

    @GetMapping("/{groupId}/chains")
    public Result<List<ChainVO>> getGroupChains(@PathVariable Long groupId) {
        return Result.success(chainService.getGroupChains(groupId));
    }

    @PostMapping("/{groupId}/chains/{chainId}/segments")
    public Result<ChainSegmentVO> addChainSegment(
            @PathVariable Long groupId,
            @PathVariable Long chainId,
            @Valid @RequestBody ChainSegmentCreateDTO dto) {
        ChainSegmentVO seg = chainService.addSegment(chainId, getCurrentUserId(), dto);
        // Auto-send group message
        GroupMessageCreateDTO msg = new GroupMessageCreateDTO();
        msg.setBody(dto.getBody() != null && dto.getBody().length() > 50
                ? dto.getBody().substring(0, 50) + "..." : dto.getBody());
        msg.setType("CHAIN_SEGMENT");
        msg.setRefId(chainId);
        msg.setRefType("CHAIN");
        messageService.sendMessage(groupId, getCurrentUserId(), msg);
        return Result.success(seg);
    }

    // ===== Writing Battles =====

    @PostMapping("/{groupId}/battles")
    public Result<BattleVO> createBattle(
            @PathVariable Long groupId,
            @Valid @RequestBody BattleCreateDTO dto) {
        BattleVO battle = battleService.createBattle(groupId, getCurrentUserId(), dto);
        // Auto-send group message
        GroupMessageCreateDTO msg = new GroupMessageCreateDTO();
        msg.setBody(dto.getTopic());
        msg.setType("BATTLE_START");
        msg.setRefId(battle.getId());
        msg.setRefType("BATTLE");
        messageService.sendMessage(groupId, getCurrentUserId(), msg);
        return Result.success(battle);
    }

    @GetMapping("/{groupId}/battles")
    public Result<List<BattleVO>> getGroupBattles(@PathVariable Long groupId) {
        return Result.success(battleService.getGroupBattles(groupId));
    }

    @GetMapping("/{groupId}/battles/{battleId}")
    public Result<BattleVO> getBattleDetail(
            @PathVariable Long groupId,
            @PathVariable Long battleId) {
        return Result.success(battleService.getBattleDetail(battleId));
    }

    @PostMapping("/{groupId}/battles/{battleId}/entries")
    public Result<BattleEntryVO> submitBattleEntry(
            @PathVariable Long groupId,
            @PathVariable Long battleId,
            @Valid @RequestBody BattleEntryDTO dto) {
        BattleEntryVO entry = battleService.submitEntry(battleId, getCurrentUserId(), dto);
        // Auto-send group message
        GroupMessageCreateDTO msg = new GroupMessageCreateDTO();
        msg.setBody(dto.getTitle());
        msg.setType("BATTLE_ENTRY");
        msg.setRefId(battleId);
        msg.setRefType("BATTLE");
        messageService.sendMessage(groupId, getCurrentUserId(), msg);
        return Result.success(entry);
    }

    @PostMapping("/{groupId}/battles/{battleId}/close")
    public Result<String> closeBattle(
            @PathVariable Long groupId,
            @PathVariable Long battleId) {
        battleService.closeBattle(battleId, getCurrentUserId());
        return Result.success("擂台已关闭，进入评审阶段");
    }

    // ===== Battle Review (standalone, not scoped under group in URL) =====

    @PostMapping("/battles/entries/{entryId}/reviews")
    public Result<String> addReview(
            @PathVariable Long entryId,
            @Valid @RequestBody BattleReviewDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        battleService.addReview(entryId, userId, dto);
        return Result.success("评审已提交");
    }

    // ===== Helper =====

    private boolean isGroupAdmin(Long groupId) {
        try {
            GroupDetailVO detail = groupService.getGroupDetail(groupId);
            return detail.getCreatorId().equals(getCurrentUserId());
        } catch (Exception e) {
            return false;
        }
    }
}
