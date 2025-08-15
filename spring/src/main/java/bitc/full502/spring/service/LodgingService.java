package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodWishRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.dto.AvailabilityDto;
import bitc.full502.spring.dto.LodgingDetailDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * 숙박 상세/집계/가용체크
 * - lod_cnt(예약점유 기록)는 건드리지 않음: 조회수 관련 쿼리 제거
 * - wish/book 집계는 각각 lod_wish / lod_book에서 계산
 */
@Service
public class LodgingService {

    private final LodgingRepository lodgingRepository;
    private final LodBookRepository lodBookRepository;
    private final LodWishRepository lodWishRepository;

    public LodgingService(LodgingRepository lodgingRepository,
                          LodBookRepository lodBookRepository,
                          LodWishRepository lodWishRepository) {
        this.lodgingRepository = lodgingRepository;
        this.lodBookRepository = lodBookRepository;
        this.lodWishRepository = lodWishRepository;
    }

    @Transactional(readOnly = true)
    public Lodging findByIdOrThrow(Long id) {
        return lodgingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lodging not found: " + id));
    }

    /** 상세 */
    @Transactional(readOnly = true)
    public LodgingDetailDto getDetail(Long id) {
        Lodging lod = findByIdOrThrow(id);

        // 조회수는 사용 안 함(0 고정)
        long views = 0L;

        // 찜/예약 집계
        long wishCount = lodWishRepository.countByLodging_Id(id);
        long bookCount = lodBookRepository.countActive(id); // CANCEL 제외 누적

        return LodgingDetailDto.builder()
                .id(lod.getId())
                .name(lod.getName())
                .city(lod.getCity())
                .town(lod.getTown())
                .vill(lod.getVill())
                .phone(lod.getPhone())
                .addrRd(lod.getAddrRd())
                .addrJb(lod.getAddrJb())
                .lat(lod.getLat())
                .lon(lod.getLon())
                .totalRoom(lod.getTotalRoom())
                .img(lod.getImg())
                .views(views)
                .wishCount(wishCount)
                .bookCount(bookCount)
                .build();
    }

    /**
     * 예약 가능 여부
     * - 겹침: existing.ckIn < 요청.checkOut AND existing.ckOut > 요청.checkIn
     * - CANCEL 제외
     * - 총 객실수(default 3)에서 겹치는 예약 수를 뺌
     */
    @Transactional(readOnly = true)
    public AvailabilityDto checkAvailability(Long lodgingId, String checkInStr, String checkOutStr, Integer guests) {
        if (checkInStr == null || checkOutStr == null) {
            return AvailabilityDto.builder()
                    .available(false)
                    .reason("checkIn/checkOut 파라미터가 필요합니다")
                    .build();
        }

        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInStr);
            checkOut = LocalDate.parse(checkOutStr);
        } catch (DateTimeParseException e) {
            return AvailabilityDto.builder()
                    .available(false)
                    .reason("날짜 형식은 YYYY-MM-DD 입니다")
                    .checkIn(checkInStr)
                    .checkOut(checkOutStr)
                    .guests(guests)
                    .build();
        }

        if (!checkIn.isBefore(checkOut)) {
            return AvailabilityDto.builder()
                    .available(false)
                    .reason("체크인은 체크아웃보다 이전이어야 합니다")
                    .checkIn(checkInStr)
                    .checkOut(checkOutStr)
                    .guests(guests)
                    .build();
        }

        Lodging lodging = findByIdOrThrow(lodgingId);
        int total = lodging.getTotalRoom() == null ? 3 : lodging.getTotalRoom(); // 기본 3

        long overlapping = lodBookRepository.countOverlapping(lodgingId, checkIn, checkOut);
        int availableRooms = Math.max(total - (int) overlapping, 0);
        boolean ok = availableRooms > 0;

        return AvailabilityDto.builder()
                .available(ok)
                .totalRoom(total)
                .reservedRooms(overlapping)
                .availableRooms(availableRooms)
                .reason(ok ? null : "요청 기간 만실입니다")
                .checkIn(checkInStr)
                .checkOut(checkOutStr)
                .guests(guests)
                .build();
    }
}
