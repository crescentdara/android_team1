//package bitc.full502.spring.service;
//
//import bitc.full502.spring.domain.entity.FlBook;
//import bitc.full502.spring.domain.entity.Flight;
//import bitc.full502.spring.domain.entity.Users;
//import bitc.full502.spring.domain.repository.FlBookRepository;
//import bitc.full502.spring.domain.repository.FlightRepository;
//import bitc.full502.spring.domain.repository.UsersRepository;
//import bitc.full502.spring.dto.BookingRequestDto;
//import bitc.full502.spring.dto.BookingResponseDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class FlBookServiceImpl implements FlBookService {
//
//    private final FlBookRepository flBookRepository;
//    private final FlightRepository flightRepository;
//    private final UsersRepository usersRepository;
//
//    @Override
//    public BookingResponseDto createBooking(BookingRequestDto req) {
//        if (req == null) throw new IllegalArgumentException("요청이 비었습니다.");
//
//        Long userId = req.getUserId();
//        Long flId   = req.getFlId();
//        LocalDate tripDate = req.getTripDate();
//        Integer adult = req.getAdult();
//        Integer child = req.getChild();
//        Integer seatCnt = req.getSeatCnt();
//        Long totalPrice = req.getTotalPrice();
//
//        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId 누락");
//        if (flId == null   || flId <= 0)   throw new IllegalArgumentException("flId 누락");
//        if (tripDate == null)              throw new IllegalArgumentException("tripDate 누락");
//        if (adult == null)                 adult = 0;
//        if (child == null)                 child = 0;
//        if (seatCnt == null || seatCnt <= 0) seatCnt = adult + child;
//        if (seatCnt <= 0) throw new IllegalArgumentException("seatCnt(=adult+child)가 0입니다.");
//
//        Users user = usersRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));
//
//        Flight flight = flightRepository.findById(flId)
//                .orElseThrow(() -> new IllegalArgumentException("항공편 없음: " + flId));
//
//        long already = flBookRepository.countBookedSeats(flId, tripDate);
//        int total = (flight.getTotalSeat() == null ? 20 : flight.getTotalSeat());
//        if (already + seatCnt > total) {
//            throw new IllegalStateException(
//                    "잔여좌석 부족: 이미 " + already + "석, 요청 " + seatCnt + "석 / 총 " + total + "석");
//        }
//
//        FlBook book = FlBook.builder()
//                .user(user)
//                .flight(flight)
//                .adult(adult)
//                .child(child)
//                .totalPrice(totalPrice == null ? 0L : totalPrice)
//                .status("PAID") // 결제 완료 저장
//                .tripDate(tripDate)
//                .build();
//
//        book = flBookRepository.save(book);
//
//        return toDto(book);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public BookingResponseDto getBooking(Long bookingId) {
//        FlBook b = flBookRepository.findById(bookingId)
//                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + bookingId));
//        return toDto(b);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<BookingResponseDto> getBookingsByUser(Long userId) {
//        Users user = usersRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));
//        return flBookRepository.findByUser(user).stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void cancelBooking(Long bookingId) {
//        FlBook b = flBookRepository.findById(bookingId)
//                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + bookingId));
//        b.setStatus("CANCEL");
//        // flush는 트랜잭션 종료 시점
//    }
//
//    private BookingResponseDto toDto(FlBook b) {
//        BookingResponseDto dto = new BookingResponseDto();
//        dto.setBookingId(b.getId());
//        dto.setUserId(b.getUser().getId());
//        dto.setFlightId(b.getFlight().getId());
//        int seatCnt = (b.getAdult() == null ? 0 : b.getAdult())
//                + (b.getChild() == null ? 0 : b.getChild());
//        dto.setSeatCnt(seatCnt);
//        dto.setAdult(b.getAdult());
//        dto.setChild(b.getChild());
//        dto.setTotalPrice(b.getTotalPrice());
//        dto.setStatus(b.getStatus());
//        dto.setTripDate(b.getTripDate());
//        return dto;
//    }
//}

package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.*;
import bitc.full502.spring.domain.repository.*;
import bitc.full502.spring.dto.*;
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

        Long userId   = req.getUserId();
        Long outFlId  = req.getOutFlId();
        Long inFlId   = req.getInFlId();     // 왕복일 경우
        LocalDate dep = req.getDepDate();    // 가는 날
        LocalDate ret = req.getRetDate();    // 오는 날(왕복만)
        Integer adult = req.getAdult();
        Integer child = req.getChild();
        Integer seatCnt = req.getSeatCnt();
        Long totalPrice = req.getTotalPrice();

        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId 누락");
        if (outFlId == null || outFlId <= 0) throw new IllegalArgumentException("outFlId 누락");
        if (dep == null) throw new IllegalArgumentException("depDate 누락");

        if (adult == null) adult = 0;
        if (child == null) child = 0;
        if (seatCnt == null || seatCnt <= 0) seatCnt = adult + child;
        if (seatCnt <= 0) throw new IllegalArgumentException("seatCnt(=adult+child)가 0입니다.");

        // 왕복 유효성
        final boolean roundTrip = (inFlId != null && inFlId > 0 && ret != null);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));

        Flight outFlight = flightRepository.findById(outFlId)
                .orElseThrow(() -> new IllegalArgumentException("가는편 항공편 없음: " + outFlId));

        Flight inFlight = null;
        if (roundTrip) {
            inFlight = flightRepository.findById(inFlId)
                    .orElseThrow(() -> new IllegalArgumentException("오는편 항공편 없음: " + inFlId));
        }

        // 좌석 점검: 가는 편
        long alreadyOut = flBookRepository.countBookedSeats(outFlId, dep);
        int totalOut = (outFlight.getTotalSeat() == null ? 20 : outFlight.getTotalSeat());
        if (alreadyOut + seatCnt > totalOut) {
            throw new IllegalStateException(
                    "가는편 잔여좌석 부족: 이미 " + alreadyOut + "석, 요청 " + seatCnt + "석 / 총 " + totalOut + "석");
        }

        // 좌석 점검: 오는 편(왕복일 때만)
        if (roundTrip) {
            long alreadyIn = flBookRepository.countBookedSeats(inFlId, ret);
            int totalIn = (inFlight.getTotalSeat() == null ? 20 : inFlight.getTotalSeat());
            if (alreadyIn + seatCnt > totalIn) {
                throw new IllegalStateException(
                        "오는편 잔여좌석 부족: 이미 " + alreadyIn + "석, 요청 " + seatCnt + "석 / 총 " + totalIn + "석");
            }
        }

        // 저장
        FlBook book = FlBook.builder()
                .user(user)
                .flight(outFlight)           // 가는 편
                .returnFlight(inFlight)      // 왕복이면 세팅
                .adult(adult)
                .child(child)
                .totalPrice(totalPrice == null ? 0L : totalPrice)
                .status("PAID")              // 결제 완료 저장
                .depDate(dep)
                .retDate(roundTrip ? ret : null)
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
    }

    private BookingResponseDto toDto(FlBook b) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(b.getId());
        dto.setUserId(b.getUser().getId());
        dto.setOutFlightId(b.getFlight() != null ? b.getFlight().getId() : null);
        dto.setInFlightId(b.getReturnFlight() != null ? b.getReturnFlight().getId() : null);

        int seatCnt = (b.getAdult() == null ? 0 : b.getAdult())
                + (b.getChild() == null ? 0 : b.getChild());
        dto.setSeatCnt(seatCnt);
        dto.setAdult(b.getAdult());
        dto.setChild(b.getChild());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setStatus(b.getStatus());
        dto.setDepDate(b.getDepDate());
        dto.setRetDate(b.getRetDate());
        return dto;
    }
}
