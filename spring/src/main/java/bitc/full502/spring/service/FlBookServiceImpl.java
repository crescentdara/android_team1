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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FlBookServiceImpl implements FlBookService {

    private final FlBookRepository flBookRepository;
    private final FlightRepository flightRepository;
    private final UsersRepository usersRepository;

    @Override
    public BookingResponseDto createBooking(BookingRequestDto req) {
        if (req == null) throw new IllegalArgumentException("요청이 비었습니다.");

        Long userId = req.getUserId();
        Long flId   = req.getFlId();
        LocalDate tripDate = req.getTripDate();
        Integer adult = req.getAdult();
        Integer child = req.getChild();
        Integer seatCnt = req.getSeatCnt();
        Long totalPrice = req.getTotalPrice();

        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId 누락");
        if (flId == null   || flId <= 0)   throw new IllegalArgumentException("flId 누락");
        if (tripDate == null)              throw new IllegalArgumentException("tripDate 누락");
        if (adult == null)                 adult = 0;
        if (child == null)                 child = 0;
        if (seatCnt == null || seatCnt <= 0) seatCnt = adult + child;
        if (seatCnt <= 0) throw new IllegalArgumentException("seatCnt(=adult+child)가 0입니다.");

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        Flight flight = flightRepository.findById(flId)
                .orElseThrow(() -> new IllegalArgumentException("항공편 없음: " + flId));

        long already = flBookRepository.countBookedSeats(flId, tripDate);
        int total = (flight.getTotalSeat() == null ? 20 : flight.getTotalSeat());
        if (already + seatCnt > total) {
            throw new IllegalStateException(
                    "잔여좌석 부족: 이미 " + already + "석, 요청 " + seatCnt + "석 / 총 " + total + "석");
        }

        FlBook book = FlBook.builder()
                .user(user)
                .flight(flight)
                .adult(adult)
                .child(child)
                .totalPrice(totalPrice == null ? 0L : totalPrice)
                .status("PAID") // 결제 완료 저장
                .tripDate(tripDate)
                .build();

        book = flBookRepository.save(book);

        return toDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long bookingId) {
        FlBook b = flBookRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + bookingId));
        return toDto(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUser(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));
        return flBookRepository.findByUser(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelBooking(Long bookingId) {
        FlBook b = flBookRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + bookingId));
        b.setStatus("CANCEL");
        // flush는 트랜잭션 종료 시점
    }

    private BookingResponseDto toDto(FlBook b) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(b.getId());
        dto.setUserId(b.getUser().getId());
        dto.setFlightId(b.getFlight().getId());
        int seatCnt = (b.getAdult() == null ? 0 : b.getAdult())
                + (b.getChild() == null ? 0 : b.getChild());
        dto.setSeatCnt(seatCnt);
        dto.setAdult(b.getAdult());
        dto.setChild(b.getChild());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setStatus(b.getStatus());
        dto.setTripDate(b.getTripDate());
        return dto;
    }
}
