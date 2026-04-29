// service/impl/RevisionServiceImpl.java
package com.example.snowball.service.impl;
import com.example.snowball.dto.RevisionCreateDTO;
import com.example.snowball.entity.Revision;
import com.example.snowball.repository.RevisionRepository;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.service.RevisionService;
import com.example.snowball.vo.RevisionVO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
@Service
public class RevisionServiceImpl implements RevisionService {
    private final RevisionRepository revisionRepository;
    private final UserRepository userRepository;
    public RevisionServiceImpl(RevisionRepository revisionRepository, UserRepository userRepository) {
        this.revisionRepository = revisionRepository;
        this.userRepository = userRepository;
    }
    @Override
    public List<RevisionVO> getRevisions(Long postId) {
        List<Revision> revisions = revisionRepository.findByOriginalPostIdOrderByCreatedAtDesc(postId);
        List<RevisionVO> voList = new ArrayList<>();
        for (Revision r : revisions) {
            RevisionVO vo = new RevisionVO();
            vo.setId(r.getId());
            vo.setOriginalPostId(r.getOriginalPostId());
            vo.setAuthorUserId(r.getAuthorUserId());
            vo.setTitle(r.getTitle());
            vo.setBody(r.getBody());
            vo.setSummary(r.getSummary());
            vo.setVoteCount(r.getVoteCount());
            vo.setCreatedAt(r.getCreatedAt());
            userRepository.findById(r.getAuthorUserId()).ifPresent(user -> vo.setAuthorName(user.getUsername()));
            voList.add(vo);
        }
        return voList;
    }
    @Override
    public RevisionVO createRevision(Long postId, Long userId, RevisionCreateDTO dto) {
        Revision rev = new Revision();
        rev.setOriginalPostId(postId);
        rev.setAuthorUserId(userId);
        rev.setTitle(dto.getTitle());
        rev.setBody(dto.getBody());
        rev.setSummary(dto.getSummary());
        rev = revisionRepository.save(rev);

        RevisionVO vo = new RevisionVO();
        vo.setId(rev.getId());
        vo.setOriginalPostId(rev.getOriginalPostId());
        vo.setAuthorUserId(rev.getAuthorUserId());
        vo.setTitle(rev.getTitle());
        vo.setBody(rev.getBody());
        vo.setSummary(rev.getSummary());
        userRepository.findById(userId).ifPresent(user -> vo.setAuthorName(user.getUsername()));
        return vo;
    }
}