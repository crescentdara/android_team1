package bitc.full502.spring.service;

import bitc.full502.spring.dto.BookingRequestDto;
import bitc.full502.spring.dto.BookingResponseDto;

import java.util.List;

public interface FlBookService {
    BookingResponseDto createBooking(BookingRequestDto req);
    BookingResponseDto getBooking(Long bookingId);
    List<BookingResponseDto> getBookingsByUser(Long userId);
    void cancelBooking(Long bookingId);
}
