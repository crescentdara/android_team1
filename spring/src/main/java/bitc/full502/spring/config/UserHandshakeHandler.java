package bitc.full502.spring.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        // 1) userId 가져오기 (쿼리 파라미터)
        String raw = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("userId");

        // 2) URL decode + trim
        String userId = (raw == null) ? null
                : URLDecoder.decode(raw, StandardCharsets.UTF_8).trim();

        // 3) 필수 검사: 비어있으면 거부(= 핸드셰이크 실패시켜 클라가 즉시 원인 파악)
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Missing required query param: userId");
            // 또는 return () -> "anonymous";  // (클라에서도 같은 "anonymous"로 접속해야만 inbox가 맞습니다)
        }

        // 4) Principal 고정
        return () -> userId;
    }
}
