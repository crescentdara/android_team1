package bitc.full502.spring.service;

import bitc.full502.spring.dto.CommDto;

import java.util.List;

public interface CommService {
    List<CommDto> list(Long postId);
    Long write(Long postId, Long parentId, String content);
    void edit(Long id, String content);
    void remove(Long id);
}
