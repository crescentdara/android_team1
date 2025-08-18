package bitc.full502.spring.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/naver/local")
public class NaverLocalProxyController {

    @Value("${naver.client-id}") private String clientId;

    @Value("${naver.client-secret}") private String clientSecret;


    private final RestTemplate rt;

    public NaverLocalProxyController() {
        // 간단 타임아웃 설정(선택)
        var reqFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        reqFactory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        reqFactory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        this.rt = new RestTemplate(reqFactory);
    }

    /**
     * 네이버 지역검색 프록시
     * - OpenAPI v1: https://openapi.naver.com/v1/search/local.json
     * - 필수: query
     * - display/start/sort 지원. 반경 파라미터는 없음(정렬은 sim|comment|random 등).
     * - 클라이언트로 lat/lon을 받지만, 여기서는 검색 키워드만 서버로 전달하고
     *   위치 기반 정렬/필터링은 클라에서 처리(이미 하고 있음).
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> nearby(
            @RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "1000") int radius, // 클라 호환용(서버 미사용)
            @RequestParam(defaultValue = "30") int size
    ) {
        // 필수 키가 빠졌으면 즉시 500 대신 명확히 안내
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("items", List.of(),
                            "error", "NAVER OpenAPI client id/secret not configured"));
        }

        try {
            String url = "https://openapi.naver.com/v1/search/local.json"
                    + "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&display=" + Math.min(size, 30)  // 최대 30 권장
                    + "&start=1"
                    + "&sort=sim"; // 필요시 comment/random 등으로 변경

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);         // ✅ 정확한 헤더
            headers.set("X-Naver-Client-Secret", clientSecret); // ✅ 정확한 헤더
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<Map> resp = rt.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (RestClientResponseException ex) {
            // 네이버에서 내려준 에러를 그대로 전달(401/403/429 등 원인 파악 용이)
            String body = ex.getResponseBodyAsString();
            return ResponseEntity.status(ex.getRawStatusCode())
                    .body(Map.of("items", List.of(),
                            "error", ex.getRawStatusCode() + " " + ex.getStatusText(),
                            "naver", body));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("items", List.of(), "error", e.getMessage()));
        }
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}
