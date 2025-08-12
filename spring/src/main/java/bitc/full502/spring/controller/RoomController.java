package bitc.full502.spring.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    public record DirectRequest(String user1, String user2) {}

    @PostMapping("/direct")
    public Map<String, String> direct(@RequestBody DirectRequest req) {
        String roomId = RoomIdUtil.directRoomId(req.user1(), req.user2());
        return Map.of("roomId", roomId);
    }
}