package com.freetsinghua.tool.core.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.JsonUtils;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;

/**
 * construct a json
 *
 * @author z.tsinghua
 * @date 2019/2/13
 */
public class EJsonWriter implements AutoCloseable {
    private final JsonGenerator generator;
    private static JsonFactory factory = new JsonFactory();
    @Nullable private Writer writer;

    static {
        factory.setCodec(new ObjectMapper());
    }

    /** 使用{@link StringWriter}，可通过调用{@link #prettyJsonString()}获取json字符串 */
    public EJsonWriter() throws IOException {
        this.writer = new StringWriter();
        this.generator = factory.createGenerator(this.writer);
//        this.generator.useDefaultPrettyPrinter();
    }

    public EJsonWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    public EJsonWriter(OutputStream out) throws IOException {
        this.generator = factory.createGenerator(out);
    }

    public EJsonWriter(Writer writer) throws IOException {
        this.generator = factory.createGenerator(writer);
    }

    public EJsonWriter(DataOutput out) throws IOException {
        this.generator = factory.createGenerator(out);
    }

    public EJsonWriter(File file, JsonEncoding encoding) throws IOException {
        this.generator = factory.createGenerator(file, encoding);
    }

    public EJsonWriter(DataOutput out, JsonEncoding encoding) throws IOException {
        this.generator = factory.createGenerator(out, encoding);
    }

    public EJsonWriter writeNumberField(String fieldName, int value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeNumberField(String fieldName, long value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeNumberField(String fieldName, float value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeNumberField(String fieldName, double value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeObject(Object value) throws IOException {
        this.generator.writeObject(value);
        return this;
    }

    public EJsonWriter writeInteger(int value) throws IOException {
        this.generator.writeNumber(value);
        return this;
    }

    public EJsonWriter writeShort(short value) throws IOException {
        this.generator.writeNumber(value);
        return this;
    }

    public EJsonWriter writeLong(long value) throws IOException {
        this.generator.writeNumber(value);
        return this;
    }

    public EJsonWriter writeFloat(float value) throws IOException {
        this.generator.writeNumber(value);
        return this;
    }

    public EJsonWriter writeDouble(double value) throws IOException {
        this.generator.writeNumber(value);
        return this;
    }

    public EJsonWriter writeNullField(String fieldName) throws IOException {
        this.generator.writeNullField(fieldName);
        return this;
    }

    public EJsonWriter writeNull() throws IOException {
        this.generator.writeNull();
        return this;
    }

    public EJsonWriter writeDoubleField(String fieldName, double value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeFloatField(String fieldName, float value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeLongField(String fieldName, long value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeIntegerField(String fieldName, int value) throws IOException {
        this.generator.writeNumberField(fieldName, value);
        return this;
    }

    public EJsonWriter writeBooleanField(String fieldName, boolean value) throws IOException {
        this.generator.writeBooleanField(fieldName, value);
        return this;
    }

    public EJsonWriter writeStringField(String fieldName, String value) throws IOException {
        this.generator.writeStringField(fieldName, value);
        return this;
    }

    public EJsonWriter writeObjectField(String fieldName, Object value) throws IOException {
        this.generator.writeObjectField(fieldName, value);
        return this;
    }

    public EJsonWriter writeStartObject() throws IOException {
        this.generator.writeStartObject();
        return this;
    }

    public EJsonWriter writeEndObject() throws IOException {
        this.generator.writeEndObject();
        return this;
    }

    public EJsonWriter writeStartArray() throws IOException {
        this.generator.writeStartArray();
        return this;
    }

    public EJsonWriter writeEndArray() throws IOException {
        this.generator.writeEndArray();
        return this;
    }

    public EJsonWriter writeFieldName(String name) throws IOException {
        this.generator.writeFieldName(name);
        return this;
    }

    public EJsonWriter writeFieldName(SerializableString name) throws IOException {
        this.generator.writeFieldName(name);
        return this;
    }

    public EJsonWriter writeArray(int[] array, int offSet, int length) throws IOException {
        this.generator.writeArray(array, offSet, length);
        return this;
    }

    public EJsonWriter writeArray(long[] array, int offSet, int length) throws IOException {
        this.generator.writeArray(array, offSet, length);
        return this;
    }

    public EJsonWriter writeArray(double[] array, int offSet, int length) throws IOException {
        this.generator.writeArray(array, offSet, length);
        return this;
    }

    public EJsonWriter writeString(Reader reader, int len) throws IOException {
        this.generator.writeString(reader, len);
        return this;
    }

    public EJsonWriter writeString(String text) throws IOException {
        this.generator.writeString(text);
        return this;
    }

    public EJsonWriter writeString(SerializableString text) throws IOException {
        this.generator.writeString(text);
        return this;
    }

    public EJsonWriter writeUTF8String(byte[] text, int offSet, int length) throws IOException {
        this.generator.writeUTF8String(text, offSet, length);
        return this;
    }

    /** 调用trycatch可自动关闭 ，否则手动调用 */
    @Override
    public void close() throws IOException {
        this.generator.close();
        if (this.writer != null) {
            this.writer.close();
        }
    }

    /**
     * 优雅的输出json字符串
     *
     * <p>只能用于缺省参数的构造函数构建的{@link EJsonWriter}
     */
    @Nullable
    public Optional<String> prettyJsonString() {
        if (this.writer != null) {
            Optional<Object> objectOptional =
                    JsonUtils.readObjectFromString(this.writer.toString(), Object.class);
            if (objectOptional.isPresent()) {
                Object obj = objectOptional.get();
                return JsonUtils.writeObjectAsPrettyString(obj);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取json字符串
     *
     * @return 返回结果
     * @throws IllegalStateException 若是{@code writer}为null，则抛出异常
     */
    public Optional<String> jsonString() throws IllegalStateException {
        if (this.writer != null) {
            return Optional.of(this.writer.toString());
        }

        return Optional.empty();
    }

    /** 等价于{@link #writeFieldName(String)} 和 {@link #writeStartArray()} */
    public EJsonWriter writeArrayFieldStart(String fieldName) throws IOException {
        this.generator.writeArrayFieldStart(fieldName);
        return this;
    }

    /** @see #writeArrayFieldStart(String) */
    public EJsonWriter writeObjectFieldStart(String fieldName) throws IOException {
        this.generator.writeObjectFieldStart(fieldName);
        return this;
    }

    public EJsonWriter writeBinaryField(String fieldName, byte[] data) throws IOException {
        this.generator.writeBinaryField(fieldName, data);
        return this;
    }

    public EJsonWriter writeBinary(byte[] data, int offset, int len) throws IOException {
        this.generator.writeBinary(data, offset, len);
        return this;
    }

    public EJsonWriter writeBinary(byte[] data) throws IOException {
        this.generator.writeBinary(data);
        return this;
    }

    public EJsonWriter writeBinary(InputStream in, int dataLength) throws IOException {
        this.generator.writeBinary(in, dataLength);
        return this;
    }
}
