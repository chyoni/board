package cwchoiit.board.common.dataserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSerializer {
    private static final ObjectMapper objectMapper = initialize();

    private static ObjectMapper initialize() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // 시간 관련 직렬화 처리를 위해
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 역직렬화 할 때 없는 필드가 있을 때 false 인 경우 에러 방지
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("[deserialize] data = {}, clazz = {}", json, clazz, e);
            return null;
        }
    }

    public static <T> T deserialize(Object json, Class<T> clazz) {
        return objectMapper.convertValue(json, clazz);
    }

    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[serialize] object = {}", object, e);
            return null;
        }
    }
}
