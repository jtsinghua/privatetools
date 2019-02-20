package com.freetsinghua.tool.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freetsinghua.tool.anotation.Nullable;

import java.io.IOException;
import java.util.Optional;

/**
 * json工具,使用jackson
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
    public static <T> Optional<T> readObjectFromString(String content, Class<T> clazz) {
        try {
            T value = objectMapper.readValue(content, clazz);
            return Optional.of(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Object转化为json字符串
     *
     * @param obj 要转化的object
     * @return 返回结果
     */
    public static Optional<String> writeObjectAsString(Object obj) {
        try {
            String jsonString = objectMapper.writeValueAsString(obj);
            return Optional.of(jsonString);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * 优雅的输出json字符串
     *
     * @param obj 对象
     * @return 返回结果
     */
    public static Optional<String> writeObjectAsPrettyString(Object obj) {
        try {
            String prettyJsonString =
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            return Optional.of(prettyJsonString);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * object转化为byte数组
     *
     * @param obj 要转化的object
     * @return 返回结果
     */
    public static Optional<byte[]> writeObjectAsBytes(Object obj) {
        Optional<String> stringOptional = writeObjectAsString(obj);
        if (stringOptional.isPresent()) {
            String value = stringOptional.get();
            return Optional.of(value.getBytes(com.freetsinghua.tool.util.StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    /**
     * 获取{@link ObjectMapper}对象
     * @return 返回结果
     */
    public static ObjectMapper getObjectMapper(){
        return objectMapper;
    }
}
