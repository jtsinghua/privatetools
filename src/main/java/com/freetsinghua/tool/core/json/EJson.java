package com.freetsinghua.tool.core.json;

import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * easy json
 *
 * @author z.tsinghua
 * @date 2019/2/13
 */
@Slf4j
public class EJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将一个指定的字符串，并转化为指定的类型
     *
     * @param content 字符串
     * @param clazz 指定的类型
     * @param <T> 类型
     * @return 返回结果
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> fromJsonString(String content, Class<T> clazz) {
        try {
            T obj = MAPPER.readValue(content, clazz);
            return ofNullable(obj);
        } catch (IOException e) {
            log.error("Convert Json string to Java type error: {}", e.getMessage(), e);
            return (Optional<T>) CommonConstant.EMPTY_OPTIONAL;
        }
    }

    /**
     * 从一个文件中读取内容，并转化为指定的类型
     *
     * @param file 文件
     * @param clazz 指定的类型
     * @param <T> 类型
     * @return 返回结果
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> fromFile(File file, Class<T> clazz) {
        try {
            T obj = MAPPER.readValue(file, clazz);
            return ofNullable(obj);
        } catch (IOException e) {
            log.error("Read content from file failed: {}", e.getMessage(), e);
            return (Optional<T>) CommonConstant.EMPTY_OPTIONAL;
        }
    }

    /**
     * 从一个{@link Reader}中读取内容，并转化为指定的类型
     *
     * @param reader 输入流
     * @param clazz 指定的类型
     * @param <T> 类型
     * @return 返回结果
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> fromReader(Reader reader, Class<T> clazz) {
        try {
            T obj = MAPPER.readValue(reader, clazz);
            return ofNullable(obj);
        } catch (IOException e) {
            log.error("Read content from Reader failed: {}", e.getMessage(), e);
            return (Optional<T>) CommonConstant.EMPTY_OPTIONAL;
        }
    }

    /**
     * 从一个输入流中读取内容，并转化为指定的类型
     *
     * @param src 输入流
     * @param clazz 指定的类型
     * @param <T> 类型
     * @return 返回结果
     */
    public static <T> Optional<T> fromInputStream(InputStream src, Class<T> clazz) {
        try {
            T obj = MAPPER.readValue(src, clazz);
            return ofNullable(obj);
        } catch (IOException e) {
            log.error("Read content from InputStream failed: {}", e.getMessage(), e);
            return empty();
        }
    }

    /**
     * 将指定的对象转化为json字符串，并写入输出流中
     *
     * @param outputStream 指定的输出流
     * @param obj 指定的对象
     * @throws IOException io异常
     */
    public static void toOutputStream(OutputStream outputStream, Object obj) throws IOException {
        MAPPER.writeValue(outputStream, obj);
    }

    /**
     * 将指定的对象转化为json字符串，并写入指定的流中
     *
     * @param writer 指定的流
     * @param obj 指定的对象
     * @throws IOException io错误
     */
    public static void toWriter(Writer writer, Object obj) throws IOException {
        MAPPER.writeValue(writer, obj);
    }

    /**
     * 将指定的对象转化为json字符串，并写入指定的文件
     *
     * @param file 指定的文件
     * @param obj 指定的对象
     * @throws IOException io错误
     */
    public static void toFile(File file, Object obj) throws IOException {
        Assert.state(file != null, "File must not be null");
        Assert.state(file.exists(), "File must exists");
        Assert.state(file.canWrite(), "File must can write");
        MAPPER.writeValue(file, obj);
    }

    /**
     * 将给定的对象转化为json字符串形式
     *
     * @param obj 给定的对象
     * @return 返回结果
     */
    public static Optional<String> toJsonString(Object obj) {
        try {
            String jsonValue = MAPPER.writeValueAsString(obj);
            return of(jsonValue);
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    /**
     * 将给定的对象转化为json字符串形式
     *
     * @param obj 给定的对象
     * @return 返回结果
     */
    public static Optional<String> toJsonString(
            Object obj, SerializationFeature... serializationFeature) {
        return getStringOptional(obj, configSerializationConfig(serializationFeature));
    }

    /**
     * 将给定的对象转化为json字符串形式
     *
     * @param obj 给定的对象
     * @return 返回结果
     */
    public static Optional<String> toJsonString(Object obj, JsonGenerator.Feature... features) {
        return getStringOptional(obj, configSerializationConfig(features));
    }

    /**
     * 将给定的对象转化为json字符串形式
     *
     * @param obj 给定的对象
     * @return 返回结果
     */
    public static Optional<String> toJsonString(Object obj, FormatFeature... features) {
        return getStringOptional(obj, configSerializationConfig(features));
    }

    private static Optional<String> getStringOptional(
            Object obj, SerializationConfig serializationConfig) {
        try {
            MAPPER.setConfig(serializationConfig);
            String jsonValue = MAPPER.writeValueAsString(obj);
            return of(jsonValue);
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    /** {@link JsonGenerator.Feature} */
    private static SerializationConfig configSerializationConfig(
            JsonGenerator.Feature... features) {
        return MAPPER.getSerializationConfig().withFeatures(features);
    }

    /** {@link FormatFeature} */
    private static SerializationConfig configSerializationConfig(FormatFeature... features) {
        return MAPPER.getSerializationConfig().withFeatures(features);
    }

    /** {@link SerializationFeature} */
    private static SerializationConfig configSerializationConfig(
            SerializationFeature... serializationFeatures) {
        return MAPPER.getSerializationConfig().withFeatures(serializationFeatures);
    }

    /**
     * 获取{@link ObjectMapper}对象，以便进行适当的定制
     *
     * @return 返回结果
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
