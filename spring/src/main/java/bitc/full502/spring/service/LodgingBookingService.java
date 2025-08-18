package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.LodBook;
import bitc.full502.spring.domain.repository.LodBookRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.LodgingBookingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LodgingBookingService {

    private final LodBookRepository bookingRepository;
    private final LodgingRepository lodgingRepository;
    private final UsersRepository usersRepository;

    public void saveBooking(LodgingBookingDto dto) {
        LodBook entity = new LodBook();

        entity.setAdult(dto.getAdult());
        entity.setChild(dto.getChild());
        entity.setCkIn(LocalDate.parse(dto.getCkIn()));
        entity.setCkOut(LocalDate.parse(dto.getCkOut()));
        entity.setRoomType(dto.getRoomType());
        entity.setStatus(dto.getStatus());
        entity.setTotalPrice(dto.getTotalPrice());

        // 외래키 매핑
        entity.setLodging(
                lodgingRepository.findById(dto.getLodId())
                        .orElseThrow(() -> new IllegalArgumentException("숙소 없음"))
        );
        entity.setUser(
                usersRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("회원 없음"))
        );

        bookingRepository.save(entity);
    }
}
