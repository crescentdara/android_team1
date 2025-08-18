package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.LodBook;
import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.LodgingBookingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class LodgingBookingService {

    private final LodBookRepository lodBookRepository;
    private final LodgingRepository lodgingRepository;
    private final UsersRepository usersRepository;

    /** 예약 저장 (총액은 서버가 객실타입 단가 × 숙박박수로 재계산) */
    public Long saveBooking(LodgingBookingDto dto) {
        LocalDate ckIn  = LocalDate.parse(dto.getCkIn());
        LocalDate ckOut = LocalDate.parse(dto.getCkOut());
        if (!ckIn.isBefore(ckOut)) {
            throw new IllegalArgumentException("체크인은 체크아웃보다 이전이어야 합니다.");
        }

        Lodging lodging = lodgingRepository.findById(dto.getLodId())
                .orElseThrow(() -> new IllegalArgumentException("숙소를 찾을 수 없습니다. id=" + dto.getLodId()));
        Users user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + dto.getUserId()));

        long overlapping = lodBookRepository.countOverlapping(lodging.getId(), ckIn, ckOut);
        int total = lodging.getTotalRoom() == null ? 3 : lodging.getTotalRoom();

        // 같은 기간에 total(기본 3)건까지 허용, 그 이상이면 만실
        if (overlapping >= total) {
            throw new IllegalStateException("요청 기간 만실입니다.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(ckIn, ckOut);
        int unit = switch (dto.getRoomType()) {
            case "싱글" -> 100_000;
            case "디럭스" -> 120_000;
            case "스위트" -> 150_000;
            default -> 0;
        };
        long totalPrice = unit * Math.max(nights, 0);

        LodBook entity = LodBook.builder()
                .adult(dto.getAdult())
                .child(dto.getChild())
                .ckIn(ckIn)
                .ckOut(ckOut)
                .roomType(dto.getRoomType())
                .status(dto.getStatus())
                .totalPrice(totalPrice)
                .lodging(lodging)
                .user(user)
                .build();

        return lodBookRepository.save(entity).getId();
    }
}
