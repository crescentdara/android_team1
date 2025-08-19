package bitc.full502.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // ✅ 서버 → 클라 브로드캐스트: /topic/* (방 메시지, 읽음영수증 모두 토픽으로 보냄)
        registry.enableSimpleBroker("/topic");

        // ✅ 클라 → 서버 전송: /app/*  (예: /app/chat.send)
        registry.setApplicationDestinationPrefixes("/app");

        // ❌ 개인 큐는 사용하지 않으므로 비활성(남겨도 되지만 혼선 방지를 위해 주석)
        // registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 안드로이드 네이티브 WebSocket 접속 (ws://<IP>:8080/ws)
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UserHandshakeHandler()) // 있으면 유지, 없으면 제거해도 OK
                .setAllowedOriginPatterns("*");

        // (옵션) SockJS 지원 — 브라우저 클라이언트용. 안드로이드는 위 엔드포인트만 있으면 충분
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
