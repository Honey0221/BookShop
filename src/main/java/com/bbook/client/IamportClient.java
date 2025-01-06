package com.bbook.client;

import com.bbook.dto.CancelData;
import com.bbook.dto.Payment;   
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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.StringHttpMessageConverter;
import java.nio.charset.StandardCharsets;

/**
 * 아임포트 결제 API와 통신하기 위한 클라이언트 클래스
 */
@Component
@groovy.util.logging.Slf4j
public class IamportClient {
    private static final Logger log = LoggerFactory.getLogger(IamportClient.class);
    private final String apiKey; // 아임포트에서 발급받은 API 키
    private final String apiSecret; // 아임포트에서 발급받은 API Secret
    private final RestTemplate restTemplate; // HTTP 요청을 보내기 위한 RestTemplate

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
     * 토큰 발급 요청을 위한 내부 DTO 클래스
     */
    private static class TokenRequest {
        @JsonProperty("imp_key")
        private String imp_key;
        @JsonProperty("imp_secret")
        private String imp_secret;

        public TokenRequest(String impKey, String impSecret) {
            this.imp_key = impKey;
            this.imp_secret = impSecret;
        }

        @JsonProperty("imp_key")
        public String getImp_key() {
            return imp_key;
        }

        @JsonProperty("imp_secret")
        public String getImp_secret() {
            return imp_secret;
        }
    }

    /**
     * 토큰 응답을 처리하기 위한 내부 DTO 클래스
     */
    private static class TokenResponse {
        private int code;
        private String message;
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

            public void setAccess_token(String access_token) {
                this.access_token = access_token;
            }
        }

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
            this.response = response;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
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
        // API 키와 시크릿 키를 이용하여 아임포트 API 토큰을 발급받는 메서드

        // 디버그 로그 - API 키와 시크릿 키 정보 기록
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
        return tokenResponse.getResponse().getAccess_token();
    }

    /**
     * 결제 정보를 조회하는 메서드
     * 
     * @param impUid 아임포트 거래 고유번호
     * @return 결제 정보가 담긴 응답 객체
     * @throws IamportResponseException 결제 정보 조회 실패시 발생
     */
    public IamportResponse<Payment> paymentByImpUid(String impUid) throws IamportResponseException {
        // 실제 구현에서는 아임포트 API를 호출
        // 현재는 테스트를 위한 더미 데이터 반환
        IamportResponse<Payment> response = new IamportResponse<>();
        Payment payment = new Payment();
        payment.setImpUid(impUid);
        response.setResponse(payment);
        return response;
    }

    /**
     * 결제를 취소하는 메서드
     * 
     * @param cancelData 취소할 결제에 대한 정보를 담은 객체
     * @return 취소된 결제 정보가 담긴 응답 객체
     * @throws IamportResponseException 결제 취소 실패시 발생
     * @throws IOException              HTTP 통신 오류 발생시
     */
    public IamportResponse<Payment> cancelPayment(CancelData cancelData) throws IamportResponseException, IOException {
        String token = this.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CancelData> entity = new HttpEntity<>(cancelData, headers);

        ResponseEntity<IamportResponse<Payment>> responseEntity = restTemplate.exchange(
                "https://api.iamport.kr/payments/cancel",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<IamportResponse<Payment>>() {
                });

        return responseEntity.getBody();
    }

    /**
     * 결제를 취소하고 환불하는 메서드
     * 
     * @param impUid       아임포트 거래 고유번호
     * @param cancelAmount 취소/환불 금액
     * @param reason       취소 사유
     * @return 결제 취소 결과가 담긴 응답 객체
     * @throws IamportResponseException 결제 취소 API 호출 실패 시 발생
     * @throws IOException              HTTP 통신 오류 발생 시
     */
    public IamportResponse<Payment> cancelPayment(String impUid, BigDecimal cancelAmount, String reason)
            throws IamportResponseException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("imp_uid", impUid);
        params.put("amount", cancelAmount);
        params.put("reason", reason);

        // API 요청 전 로그 추가
        log.debug("Cancel payment request - params: {}", params);

        return this.post("/payments/cancel", params);
    }

    private IamportResponse<Payment> post(String path, Map<String, Object> params)
            throws IamportResponseException, IOException {
        String token = this.getToken();
        String url = "https://api.iamport.kr" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ObjectMapper를 사용하여 Map을 JSON 문자열로 변환
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(params);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        log.debug("Request URL: {}", url);
        log.debug("Request Headers: {}", headers);
        log.debug("Request Body JSON: {}", jsonBody);

        ResponseEntity<IamportResponse<Payment>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<IamportResponse<Payment>>() {
                });

        log.debug("Response Status: {}", responseEntity.getStatusCode());
        log.debug("Response Body: {}", responseEntity.getBody());

        return responseEntity.getBody();
    }
}