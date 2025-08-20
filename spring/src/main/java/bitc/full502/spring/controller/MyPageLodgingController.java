package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodWishRepository;
import bitc.full502.spring.dto.LodgingBookingDto;
import bitc.full502.spring.dto.LodgingListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage/lodging")
@RequiredArgsConstructor
public class MyPageLodgingController {

    private final LodBookRepository lodBookRepository;
    private final LodWishRepository lodWishRepository;

    /** 1) 숙박 예약내역 조회 */
    // bitc.full502.spring.controller.MyPageLodgingController

    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public List<LodgingBookingDto> getBookings(@RequestParam Long userId) {
        return lodBookRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .map(b -> {
                    var l = b.getLodging();
                    LodgingBookingDto dto = new LodgingBookingDto();
                    dto.setId(b.getId());                         // ✅ 예약 PK
                    dto.setUserId(b.getUser().getId());
                    dto.setLodId(l.getId());
                    dto.setCkIn(b.getCkIn().toString());
                    dto.setCkOut(b.getCkOut().toString());
                    dto.setAdult(b.getAdult());
                    dto.setChild(b.getChild());
                    dto.setRoomType(b.getRoomType());
                    dto.setTotalPrice(b.getTotalPrice());
                    dto.setStatus(b.getStatus());

                    // ✅ 숙소 정보(리스트 썸네일/텍스트용)
                    dto.setLodName(l.getName());
                    dto.setLodImg(l.getImg());
                    dto.setAddrRd(l.getAddrRd());
                    dto.setAddrJb(l.getAddrJb());
                    return dto;
                })
                .toList();
    }



    /** 2) 숙소 즐겨찾기 조회 */
    @GetMapping("/wishlist")
    public List<LodgingListDto> getWishlist(@RequestParam Long userId) {
        return lodWishRepository.findByUser_Id(userId).stream()
                .map(w -> LodgingListDto.builder()
                        .id(w.getLodging().getId())
                        .name(w.getLodging().getName())
                        .city(w.getLodging().getCity())
                        .town(w.getLodging().getTown())
                        .addrRd(w.getLodging().getAddrRd())
                        .img(w.getLodging().getImg())
                        .basePrice(w.getLodging().getBasePrice())
                        .build())
                .toList();
    }



}


