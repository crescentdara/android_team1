package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.FlBook;
import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.FlBookRepository;
import bitc.full502.spring.domain.repository.FlightRepository;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.BookingRequestDto;
import bitc.full502.spring.dto.BookingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FlBookServiceImpl implements FlBookService {
    private final FlBookRepository flBookRepository;
    private final UsersRepository usersRepository;
    private final FlightRepository flightRepository;

    @Override
    public BookingResponseDto createBooking(BookingRequestDto req) {
        // 1) 사용자 확인
        Users user = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) 항공편 확인
        Flight flight = flightRepository.findById(req.getFlId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        // 3) (선택) 좌석/중복 검사 — 현재 스키마에 재고 정보가 없으므로 간단 검증만 수행
        if (req.getSeatCnt() <= 0) {
            throw new IllegalArgumentException("Invalid seat count");
        }

        // 4) 예약 엔티티 생성
        FlBook booking = FlBook.builder()
                .user(user)
                .flight(flight)
                .adult(req.getAdult())
                .child(req.getChild())
                .totalPrice(req.getTotalPrice())
                .status("BOOKED")
                .tripDate(req.getTripDate())
                .build();

        FlBook saved = flBookRepository.save(booking);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long bookingId) {
        FlBook b = flBookRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return mapToDto(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUser(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return flBookRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelBooking(Long bookingId) {
        FlBook b = flBookRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        // 상태 변경 -> 취소
        b.setStatus("CANCELLED");
        flBookRepository.save(b);
    }

    private BookingResponseDto mapToDto(FlBook b) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(b.getId());
        dto.setUserId(b.getUser().getId());
        dto.setFlightId(b.getFlight().getId());
        dto.setAdult(b.getAdult());
        dto.setChild(b.getChild());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setStatus(b.getStatus());
        dto.setTripDate(b.getTripDate());
        return dto;
    }
}
