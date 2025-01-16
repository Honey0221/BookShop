package com.bbook.client;

import com.bbook.dto.CancelData;
import com.bbook.dto.PaymentDto;
import com.bbook.exception.IamportResponseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.StringHttpMessageConverter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * 아임포트 결제 API와 통신하기 위한 클라이언트 클래스
 */
@Component
@groovy.util.logging.Slf4j
public class IamportClient {
    private static final Logger log = LoggerFactory.getLogger(IamportClient.class);
    private static final String API_URL = "https://api.iamport.kr";
    private final String apiKey; // 아임포트에서 발급받은 API 키
    private final String apiSecret; // 아임포트에서 발급받은 API Secret
    private final RestTemplate restTemplate; // HTTP 요청을 보내기 위한 RestTemplate
    private String cachedToken;
    private long tokenExpirationTime;
    private static final long TOKEN_VALIDITY_PERIOD = 1000 * 60 * 30; // 30분

    /**
     * IamportClient 생성자
     * 
     * @param apiKey       아임포트 API 키
     * @param apiSecret    아임포트 API Secret
     * @param restTemplate HTTP 요청을 위한 RestTemplate 객체
     */
    public IamportClient(String apiKey, String apiSecret, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.restTemplate = restTemplate;
    }

    /**
     * 토큰 응답을 처리하기 위한 내부 DTO 클래스
     */
    private static class TokenResponse {
        private Response response;

        private static class Response {
            @JsonProperty("access_token")
            private String access_token;
            @JsonProperty("expired_at")
            private long expired_at;
            @JsonProperty("now")
            private long now;

            @JsonProperty("access_token")
            public String getAccess_token() {
                return access_token;
            }
        }

        public Response getResponse() {
            return response;
        }
    }

    /**
     * 아임포트 API 접근을 위한 인증 토큰을 발급받는 메서드
     * 
     * @return 발급받은 액세스 토큰
     * @throws IamportResponseException 토큰 발급 실패시 발생
     * @throws IOException              HTTP 통신 오류 발생시
     */
    private String getToken() throws IamportResponseException, IOException {
        // 캐시된 토큰이 있고 아직 유효한 경우 재사용
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            log.debug("Using cached token");
            return cachedToken;
        }

        // 디버그 로그 - API 키와 시크릿 키 정보 기록
        log.debug("Requesting new token");

        // API 키와 시크릿 키를 이용하여 아임포트 API 토큰을 발급받는 메서드

        // 디버그 로그 - API 키와 시크릿 키키 정보 기록
        log.debug("Attempting to get token with apiKey: {}", apiKey);
        log.debug("Attempting to get token with apiSecret: {}", apiSecret);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디에 들어갈 API 키와 시크릿 키를 Map으로 구성
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("imp_key", this.apiKey);
        requestMap.put("imp_secret", this.apiSecret);

        // Map을 JSON 문자열로 변환
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(requestMap);

        // 디버그 로그 - 요청 바디 내용 기록
        log.debug("Token request body: {}", jsonBody);

        // HTTP 요청 엔티티 생성
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        // RestTemplate에 UTF-8 인코딩 설정 추가
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // 아임포트 API로 토큰 발급 요청 전송
        log.debug("Sending token request to Iamport API");
        ResponseEntity<TokenResponse> responseEntity = restTemplate.postForEntity(
                "https://api.iamport.kr/users/getToken",
                entity,
                TokenResponse.class);

        // 디버그 로그 - 응답 상태 코드와 바디 기록
        log.debug("Token response received: {}", responseEntity.getStatusCode());
        log.debug("Token response body: {}", responseEntity.getBody());

        // 응답 검증 및 토큰 추출
        TokenResponse tokenResponse = responseEntity.getBody();
        if (tokenResponse == null ||
                tokenResponse.getResponse() == null ||
                tokenResponse.getResponse().getAccess_token() == null) {
            throw new IamportResponseException("Failed to get access token");
        }

        // 발급받은 액세스 토큰 반환
        cachedToken = tokenResponse.getResponse().getAccess_token();
        tokenExpirationTime = System.currentTimeMillis() + TOKEN_VALIDITY_PERIOD;
        log.debug("New token cached, valid until: {}", new Date(tokenExpirationTime));

        return cachedToken;
    }

    /**
     * 결제를 취소하는 메서드
     * 
     * @param cancelData 취소할 결제에 대한 정보를 담은 객체
     * @return 취소된 결제 정보가 담긴 응답 객체
     * @throws IamportResponseException 결제 취소 실패시 발생
     * @throws IOException              HTTP 통신 오류 발생시
     */
    public IamportResponse<PaymentDto> cancelPayment(CancelData cancelData)
            throws IamportResponseException, IOException {
        // 파라미터 유효성 검증
        if ((cancelData.getImp_uid() == null || cancelData.getImp_uid().isEmpty()) &&
                (cancelData.getMerchant_uid() == null || cancelData.getMerchant_uid().isEmpty())) {
            throw new IamportResponseException("imp_uid 또는 merchant_uid 중 하나는 반드시 지정되어야 합니다.");
        }

        try {
            // 아임포트 API 인증 토큰 발급 받기
            String token = this.getToken();

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 취소 요청 전 로깅
            log.debug("Canceling payment with data: {}", cancelData);

            HttpEntity<CancelData> entity = new HttpEntity<>(cancelData, headers);

            ResponseEntity<IamportResponse<PaymentDto>> responseEntity = restTemplate.exchange(
                    "https://api.iamport.kr/payments/cancel",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<IamportResponse<PaymentDto>>() {
                    });

            // 응답 로깅
            log.debug("Cancel payment response: {}", responseEntity.getBody());

            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Payment cancellation failed", e);
            throw e;
        }
    }

    public IamportResponse<PaymentDto> paymentByImpUid(String impUid) {
        String url = API_URL + "/payments/" + impUid;

        try {
            String token = this.getToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<IamportResponse<PaymentDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<IamportResponse<PaymentDto>>() {
                    });

            return response.getBody();
        } catch (Exception e) {
            log.error("결제 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("결제 정보 조회 실패", e);
        }
    }

    /**
     * 여러 결제를 순차적으로 취소하는 메서드
     * 
     * @param cancelDataList 취소할 결제들의 정보 리스트
     * @return 취소된 결제들의 정보가 담긴 응답 객체 리스트
     * @throws IamportResponseException 결제 취소 실패시 발생
     * @throws IOException              HTTP 통신 오류 발생시
     */
    public List<IamportResponse<PaymentDto>> cancelPayments(List<CancelData> cancelDataList)
            throws IamportResponseException, IOException {
        List<IamportResponse<PaymentDto>> responses = new ArrayList<>();
        List<String> failedPayments = new ArrayList<>();

        for (CancelData cancelData : cancelDataList) {
            try {
                IamportResponse<PaymentDto> response = cancelPayment(cancelData);
                responses.add(response);

                if (response.getCode() != 0) {
                    failedPayments.add(String.format("주문번호: %s, 사유: %s",
                            cancelData.getMerchant_uid(), response.getMessage()));
                }

                // API 호출 간 짧은 간격 추가
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Failed to cancel payment: {}", cancelData.getMerchant_uid(), e);
                failedPayments.add(String.format("주문번호: %s, 사유: %s",
                        cancelData.getMerchant_uid(), e.getMessage()));
            }
        }

        // 실패한 결제가 있다면 예외 발생
        if (!failedPayments.isEmpty()) {
            String errorMessage = "일부 결제 취소에 실패했습니다:\n" + String.join("\n", failedPayments);
            throw new IamportResponseException(errorMessage);
        }

        return responses;
    }
}