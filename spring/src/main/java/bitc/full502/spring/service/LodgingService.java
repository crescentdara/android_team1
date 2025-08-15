package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodCntRepository;
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
 * 숙박 상세/집계/가용체크 비즈니스 로직
 * - 기존 코드는 삭제하지 않고 본 구현으로 교체 사용 가능
 * - 패키지 경로가 다르면 import 및 package만 프로젝트에 맞게 수정하세요
 */
@Service
public class LodgingService {

    private final LodgingRepository lodgingRepository;
    private final LodCntRepository lodCntRepository;
    private final LodBookRepository lodBookRepository;

    public LodgingService(LodgingRepository lodgingRepository,
                          LodCntRepository lodCntRepository,
                          LodBookRepository lodBookRepository) {
        this.lodgingRepository = lodgingRepository;
        this.lodCntRepository = lodCntRepository;
        this.lodBookRepository = lodBookRepository;
    }

    private static Long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static <T> T safe(SupplierWithException<T> s, T def) {
        try {
            return s.get();
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Lodging 단건 조회(없으면 404)
     */
    @Transactional(readOnly = true)
    public Lodging findByIdOrThrow(Long id) {
        return lodgingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lodging not found: " + id));
    }

    /**
     * 상세 진입 시 조회수 +1
     * - lod_cnt 행이 없으면 0으로 생성 후 증가
     */
    @Transactional
    public void increaseViewCount(Long lodgingId) {
        lodCntRepository.ensureCounterRow(lodgingId);
        lodCntRepository.incrementViews(lodgingId);
    }

    /* ---------- 유틸 ---------- */

    /**
     * 상세 보기 응답 조립
     * - 조회수 증가 반영 후 DTO로 변환
     * - wish/book 카운트는 테이블이 존재할 때만 계산(없으면 0)
     */
    @Transactional
    public LodgingDetailDto getDetail(Long id) {
        Lodging lod = findByIdOrThrow(id);

        // 조회수 증가
        increaseViewCount(id);

        // 집계값 조회
        Long views = safe(() -> lodCntRepository.getViews(id), 0L);
        Long wishCount = safe(() -> lodCntRepository.countWish(id), 0L);
        Long bookCount = safe(() -> lodCntRepository.countBooking(id), 0L);

        // DTO 변환
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
                .views(nvl(views))
                .wishCount(nvl(wishCount))
                .bookCount(nvl(bookCount))
                .build();
    }

    /**
     * 예약 가능 여부 체크
     * - 겹침 조건: existing.check_in < 요청.checkOut AND existing.check_out > 요청.checkIn
     * - CANCEL 상태 예약은 제외
     */
    @Transactional(readOnly = true)
    public AvailabilityDto checkAvailability(Long lodgingId, String checkInStr, String checkOutStr, Integer guests) {
        // 입력 검증
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

        // 숙소 존재 확인
        Lodging lodging = findByIdOrThrow(lodgingId);
        int total = lodging.getTotalRoom() == null ? 0 : lodging.getTotalRoom();

        // 기간 겹침 예약 수
        long overlapping = lodBookRepository.countOverlapping(lodgingId, checkIn, checkOut);

        // 남은 객실 계산
        int availableRooms = Math.max(total - (int) overlapping, 0);
        boolean ok = availableRooms > 0;

        return AvailabilityDto.builder()
                .available(ok)
                .totalRoom(total)
                .reservedRooms(overlapping)
                .availableRooms(availableRooms)
                .reason(ok ? null : (total == 0 ? "총 객실 수가 0입니다" : "요청 기간 만실입니다"))
                .checkIn(checkInStr)
                .checkOut(checkOutStr)
                .guests(guests)
                .build();
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}