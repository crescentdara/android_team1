package bitc.full502.spring.config;

import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD

import org.springframework.context.annotation.Configuration;
=======
>>>>>>> origin/jgy/Flight
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

<<<<<<< HEAD
    // 클라이언트가 메시지를 보낼 경로 prefix (예: /app/chat.send)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 서버가 클라이언트에게 푸시할 때 사용할 간단 브로커(prefix)
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라이언트 → 서버(컨트롤러 @MessageMapping)로 보낼 때 붙일 prefix
        registry.setApplicationDestinationPrefixes("/app");
        // (선택) 개인 큐 prefix — 나중에 사용자별 큐 쓰고 싶을 때
        registry.setUserDestinationPrefix("/user");
    }

    // 실제 WebSocket 접속 엔드포인트 (STOMP handshake)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UserHandshakeHandler()) // userId를 Principal로 심어줌
                .setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UserHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

=======
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");  // 개발중이면 허용
        // SockJS 쓰면 .withSockJS(); 추가
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 구독 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 send prefix
        registry.setUserDestinationPrefix("/user"); // convertAndSendToUser 용
    }
>>>>>>> origin/jgy/Flight
}
