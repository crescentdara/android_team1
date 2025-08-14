package bitc.full502.spring.dto;
import java.time.Instant;

public record ReadReceiptDTO(String roomId, String readerId, Long lastReadId, Instant at) {}

