package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodWishRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.dto.AvailabilityDto;
import bitc.full502.spring.dto.LodgingDetailDto;
import bitc.full502.spring.dto.LodgingListDto;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
                    .available(false).reason("checkIn/checkOut 파라미터가 필요합니다").build();
        }

        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInStr);
            checkOut = LocalDate.parse(checkOutStr);
        } catch (DateTimeParseException e) {
            return AvailabilityDto.builder()
                    .available(false)
                    .reason("날짜 형식은 YYYY-MM-DD 입니다")
                    .checkIn(checkInStr).checkOut(checkOutStr).guests(guests)
                    .build();
        }

        if (!checkIn.isBefore(checkOut)) {
            return AvailabilityDto.builder()
                    .available(false)
                    .reason("체크인은 체크아웃보다 이전이어야 합니다")
                    .checkIn(checkInStr).checkOut(checkOutStr).guests(guests)
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
                .reason(ok ? null : (total == 0 ? "총 객실 수가 0입니다" : "요청 기간 만실입니다"))
                .checkIn(checkInStr).checkOut(checkOutStr).guests(guests)
                .build();
    }

    /**
     * 🔍 조건 검색 + 페이지네이션 (내부 필터 로직)
     * - city / town(CSV) / vill(CSV) / 날짜(가용객실) 필터
     */
    @Transactional(readOnly = true)
    public Page<Lodging> searchLodgings(
            String city,
            String town,
            String vill,
            String checkIn,
            String checkOut,
            Integer adults,
            Integer children,
            Pageable pageable
    ) {
        List<Lodging> lodgings = lodgingRepository.findAll();

        if (city != null && !city.isBlank()) {
            lodgings = lodgings.stream()
                    .filter(l -> city.equals(l.getCity()))
                    .toList();
        }

        if (town != null && !town.isBlank()) {
            Set<String> towns = Arrays.stream(town.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            lodgings = lodgings.stream()
                    .filter(l -> towns.isEmpty() || towns.contains(l.getTown()))
                    .toList();
        }

        if (vill != null && !vill.isBlank()) {
            Set<String> vills = Arrays.stream(vill.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            lodgings = lodgings.stream()
                    .filter(l -> vills.isEmpty() || vills.contains(l.getVill()))
                    .toList();
        }

        if (checkIn != null && checkOut != null && !checkIn.isBlank() && !checkOut.isBlank()) {
            LocalDate ci = LocalDate.parse(checkIn);
            LocalDate co = LocalDate.parse(checkOut);
            lodgings = lodgings.stream()
                    .filter(l -> {
                        long overlapping = lodBookRepository.countOverlapping(l.getId(), ci, co);
                        int total = (l.getTotalRoom() != null ? l.getTotalRoom() : 0);
                        int availableRooms = total - (int) overlapping;
                        return availableRooms > 0;
                    })
                    .toList();
        }

        // 페이지네이션
        if (pageable == null) pageable = PageRequest.of(0, 30, Sort.by("name").ascending());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), lodgings.size());
        List<Lodging> pageContent = start > end ? Collections.emptyList() : lodgings.subList(start, end);
        return new PageImpl<>(pageContent, pageable, lodgings.size());
    }

    /**
     * 2페이지용 목록 DTO 반환 (사진/이름/주소/가격)
     * - Android 2페이지에서 바로 사용
     * - ✅ 핵심: 엔티티 basePrice(Integer) → DTO price(Long) 명시 매핑
     */
    @Transactional(readOnly = true)
    public List<LodgingListDto> findLodgingsAsList(
            String city,
            String town,
            String vill,
            String checkIn,
            String checkOut,
            Integer adults,
            Integer children
    ) {
        // 적절한 기본 페이지 크기(예: 100)
        Page<Lodging> page = searchLodgings(
                city, town, vill, checkIn, checkOut, adults, children,
                PageRequest.of(0, 100, Sort.by("name").ascending())
        );

        return page.getContent().stream()
                .map(this::toListDto)
                .toList();
    }

    /**
     * Lodging → LodgingListDto 변환
     * - ✅ price = basePrice(Long)로 변환하여 Android가 기대하는 "price" 필드에 채움
     */
    private LodgingListDto toListDto(Lodging l) {
        String addr = (l.getAddrRd() != null && !l.getAddrRd().isBlank())
                ? l.getAddrRd()
                : l.getAddrJb();
        Long price = (l.getBasePrice() != null) ? l.getBasePrice().longValue() : 0L; // 기본가(없으면 0)

        return LodgingListDto.builder()
                .id(l.getId())
                .name(l.getName())
                .city(l.getCity())
                .town(l.getTown())
                .addrRd(addr)
                .price(price) // ★ 핵심
                .img(l.getImg())
                .build();
    }
}
