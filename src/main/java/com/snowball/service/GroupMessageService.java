package com.snowball.service;

import com.snowball.dto.GroupMessageCreateDTO;
import com.snowball.vo.GroupMessageVO;
import java.util.List;

public interface GroupMessageService {
    List<GroupMessageVO> getMessages(Long groupId, Long sinceId);
    GroupMessageVO sendMessage(Long groupId, Long userId, GroupMessageCreateDTO dto);
    void deleteMessage(Long groupId, Long messageId, Long userId, boolean isAdmin);
}
