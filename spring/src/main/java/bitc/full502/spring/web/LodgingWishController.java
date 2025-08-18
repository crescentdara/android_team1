package bitc.full502.spring.web;

import bitc.full502.spring.domain.entity.LodWish;
import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.LodWishRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.dto.WishStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lodging/{lodgingId}")
@RequiredArgsConstructor
public class LodgingWishController {

    private final LodWishRepository lodWishRepository;
    private final LodgingRepository lodgingRepository;

    private Lodging getLodgingOr404(Long id) {
        return lodgingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lodging not found: " + id));
    }

    /** 현재 찜 상태/개수 조회 */
    @GetMapping("/wish")
    public WishStatusDto getWish(@PathVariable Long lodgingId, @RequestParam Long userId) {
        boolean wished = lodWishRepository.existsByUser_IdAndLodging_Id(userId, lodgingId);
        long count = lodWishRepository.countByLodging_Id(lodgingId);
        return new WishStatusDto(wished, count);
    }

    /** 찜 토글 (아웃라인→채움 / 채움→아웃라인) */
    @PostMapping("/wish/toggle")
    @Transactional
    public WishStatusDto toggleWish(@PathVariable Long lodgingId, @RequestParam Long userId) {
        // 존재 확인
        Lodging lodging = getLodgingOr404(lodgingId);

        // 이미 있으면 삭제(해제)
        var existing = lodWishRepository.findByUser_IdAndLodging_Id(userId, lodgingId);
        if (existing.isPresent()) {
            lodWishRepository.delete(existing.get());
        } else {
            // 없으면 생성 (Users는 최소한 id만 세팅해서 참조)
            Users userRef = Users.builder().id(userId).build();
            LodWish wish = LodWish.builder().user(userRef).lodging(lodging).build();
            lodWishRepository.save(wish);
        }

        boolean nowWished = lodWishRepository.existsByUser_IdAndLodging_Id(userId, lodgingId);
        long count = lodWishRepository.countByLodging_Id(lodgingId);
        return new WishStatusDto(nowWished, count);
    }
}
