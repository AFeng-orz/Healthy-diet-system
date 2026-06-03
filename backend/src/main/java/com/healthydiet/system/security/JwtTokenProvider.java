package com.healthydiet.system.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthydiet.system.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.jwt.secret:healthy-diet-local-dev-secret-change-me}")
    private String secret;

    @Value("${app.jwt.expire-minutes:1440}")
    private Long expireMinutes;

    public String createToken(Long userId, String username) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("exp", Instant.now().plusSeconds(expireMinutes * 60).getEpochSecond());

            String headerText = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadText = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = headerText + "." + payloadText;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception ex) {
            throw new BusinessException(500, "Token生成失败");
        }
    }

    public JwtClaims parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(401, "登录状态无效");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new BusinessException(401, "登录状态无效");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }

            Long userId = ((Number) payload.get("userId")).longValue();
            String username = String.valueOf(payload.get("username"));
            return new JwtClaims(userId, username);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(401, "登录状态无效");
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(keySpec);
        return base64UrlEncode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}