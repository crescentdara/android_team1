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
        Users user = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Flight flight = flightRepository.findById(req.getFlId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        int adult = req.getAdult() == null ? 0 : req.getAdult();
        int child = req.getChild() == null ? 0 : req.getChild();
        if (adult + child <= 0) throw new IllegalArgumentException("No passengers");
        if (req.getTripDate() == null) throw new IllegalArgumentException("tripDate required");
        if (req.getTotalPrice() == null || req.getTotalPrice() < 0) throw new IllegalArgumentException("Invalid totalPrice");

        FlBook booking = FlBook.builder()
                .user(user)
                .flight(flight)
                .adult(adult)
                .child(child)
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
        int seatCnt = (b.getAdult() == null ? 0 : b.getAdult())
                + (b.getChild() == null ? 0 : b.getChild());
        dto.setSeatCnt(seatCnt); // ★ adult + child
        dto.setTotalPrice(b.getTotalPrice());
        dto.setStatus(b.getStatus());
        dto.setTripDate(b.getTripDate());
        return dto;
    }
}
