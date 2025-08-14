package bitc.full502.spring.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userId = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("userId");
        String finalId = (userId != null && !userId.isBlank())
                ? userId
                : UUID.randomUUID().toString();
        return () -> finalId; // Principal.getName() = userId
    }
}