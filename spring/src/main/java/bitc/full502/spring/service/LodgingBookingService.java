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

@Service
@RequiredArgsConstructor
@Transactional
public class LodgingBookingService {

    private final LodBookRepository lodBookRepository;
    private final LodgingRepository lodgingRepository;
    private final UsersRepository usersRepository;

    /** 예약 저장 (yyyy-MM-dd 형식 필요) */
    public Long saveBooking(LodgingBookingDto dto) {
        // 1) DTO → 타입 변환
        LocalDate ckIn  = LocalDate.parse(dto.getCkIn());   // e.g. "2025-08-16"
        LocalDate ckOut = LocalDate.parse(dto.getCkOut());

        // 2) FK 로드
        Lodging lodging = lodgingRepository.findById(dto.getLodId())
                .orElseThrow(() -> new IllegalArgumentException("숙소를 찾을 수 없습니다. id=" + dto.getLodId()));
        Users user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + dto.getUserId()));

        // 3) 기간 겹침 검사 (CANCEL 제외)
        long overlap = lodBookRepository.countOverlapping(lodging.getId(), ckIn, ckOut);
        if (overlap > 0) {
            throw new IllegalStateException("이미 예약된 기간입니다.");
        }

        // 4) 엔티티 생성/저장
        LodBook entity = LodBook.builder()
                .adult(dto.getAdult())
                .child(dto.getChild())
                .ckIn(ckIn)
                .ckOut(ckOut)
                .roomType(dto.getRoomType())
                .status(dto.getStatus())
                .totalPrice(dto.getTotalPrice())
                .lodging(lodging)
                .user(user)
                .build();

        return lodBookRepository.save(entity).getId();
    }
}
