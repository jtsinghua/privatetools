package com.freetsinghua.tool.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freetsinghua.tool.anotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * json工具
 *
 * @author z.tsinghua
 * @date 2019/1/25
 */
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从json字符串解析对象
     *
     * @param content json字符串
     * @param clazz 要解析的类型
     * @param <T> 解析的类型
     * @return 返回解析结果，可能为null
     */
    @Nullable
    public static <T> T readObjectFromString(String content, Class<T> clazz) {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonMappingException | JsonParseException e) {
            throw new RuntimeException(
                    "the input JSON structure does not match structure expected for result type (or has other mismatch issues)",
                    e);
        } catch (IOException e) {
            throw new RuntimeException("a low-level I/O error", e);
        }
    }

    /**
     * Object转化为json字符串
     *
     * @param obj 要转化的object
     * @return 返回结果
     */
    public static String writeObjectAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * object转化为byte数组
     *
     * @param obj 要转化的object
     * @return 返回结果
     */
    public static byte[] writeObjectAsBytes(Object obj) {
        return writeObjectAsString(obj).getBytes(StandardCharsets.UTF_8);
    }
}
