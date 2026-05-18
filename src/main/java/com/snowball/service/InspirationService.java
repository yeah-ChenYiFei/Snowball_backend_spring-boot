package com.snowball.service;

import com.snowball.dto.InspirationCreateDTO;
import com.snowball.dto.InspirationUpdateDTO;
import com.snowball.vo.InspirationVO;
import java.util.List;

public interface InspirationService {
    List<InspirationVO> getMyInspirations(Long userId);
    InspirationVO addInspiration(Long userId, InspirationCreateDTO dto);
    InspirationVO updateInspiration(Long id, Long userId, InspirationUpdateDTO dto);
    void deleteInspiration(Long id, Long userId);
}
