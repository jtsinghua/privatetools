package com.freetsinghua.tool.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * @author z.tsinghua
 * @date 2019/2/15
 */
public class DefaultEjson {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static DefaultEjson defaultEjson = new DefaultEjson();

    private DefaultEjson() {}

    public static DefaultEjson getInstance() {
        return defaultEjson;
    }

    public Object parse(String text) throws IOException {
        return objectMapper.readValue(text, Object.class);
    }

    public Object parse(String text, ParserConfig config) throws IOException {
        this.objectMapper.setConfig(config.getSerializationConfig());
        this.objectMapper.setConfig(config.getDeserializationConfig());
        return parse(text);
    }

    public Object parse(String text, SerializationConfig serializationConfig) throws IOException {
        return parse(text, serializationConfig, null);
    }

    public Object parse(String text, DeserializationConfig deserializationConfig)
            throws IOException {
        return parse(text, null, deserializationConfig);
    }

    public Object parse(
            String text,
            @Nullable SerializationConfig serializationConfig,
            @Nullable DeserializationConfig deserializationConfig)
            throws IOException {
        if (serializationConfig != null) {
            this.objectMapper.setConfig(serializationConfig);
        }

        if (deserializationConfig != null) {
            this.objectMapper.setConfig(deserializationConfig);
        }

        return parse(text);
    }

    public Object parse(String text, ParserConfig config, JsonFactory.Feature features)
            throws IOException {
        this.objectMapper.setConfig(config.getSerializationConfig());
        this.objectMapper.setConfig(config.getDeserializationConfig());
        this.objectMapper.getFactory().enable(features);

        return parse(text);
    }

    public Object parse(String text, JsonFactory.Feature features) throws IOException {
        this.objectMapper.getFactory().enable(features);
        return parse(text);
    }

    public Object parse(byte[] input, JsonFactory.Feature... features) throws IOException {
        configJsonFactory(features);

        return this.objectMapper.readValue(input, Object.class);
    }

    public Object parse(
            byte[] input, int off, int len, Charset charset, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);

        String text = new String(input, off, len, charset);
        return parse(text);
    }

    public Object parse(
            byte[] input, int off, int len, Charset charset, JsonFactory.Feature features) {
        return parse(input, off, len, charset, features);
    }

    public Object parse(String text, JsonFactory.Feature... features) throws IOException {
        configJsonFactory(features);
        return parse(text);
    }

    public JSONObject parseObject(String text, JsonFactory.Feature... features) throws IOException {
        configJsonFactory(features);
        JsonNode jsonNode = this.objectMapper.readTree(text);
        Map<String, Object> map = new HashMap<>(10);
        jsonNode.fieldNames()
                .forEachRemaining(
                        fieldName -> {
                            JsonNode node = jsonNode.get(fieldName);
                            map.put(fieldName, node.toString());
                        });
        return new JSONObject(map);
    }

    public JSONObject parseObject(String text) throws IOException {
        return new JSONObject(text);
    }

    public <T> T parseObject(String json, Class<T> clazz, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);
        return this.objectMapper.readValue(json, clazz);
    }

    public <T> T parseObject(String json, JavaType type, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);
        return this.objectMapper.readValue(json, type);
    }

    public <T> T parseObject(
            String input, JavaType type, ParserConfig config, JsonFactory.Feature... features)
            throws IOException {
        this.objectMapper.setConfig(config.getDeserializationConfig());
        this.objectMapper.setConfig(config.getSerializationConfig());

        configJsonFactory(features);
        return this.objectMapper.readValue(input, type);
    }

    public <T> T parseObject(
            byte[] bytes,
            int offset,
            int len,
            Charset charset,
            JavaType type,
            JsonFactory.Feature... features)
            throws IOException {
        String content = new String(bytes, offset, len, charset);
        return parseObject(content, type, features);
    }

    public <T> T parseObject(InputStream is, JavaType type, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);
        return this.objectMapper.readValue(is, type);
    }

    public <T> T parseObject(String text, Class<T> clazz) throws IOException {
        return this.objectMapper.readValue(text, clazz);
    }

    /** {"score":[100.0,89.0,99.0,89.5,98.0]} */
    public JSONArray parseArray(String text) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(text);
        JSONArray jsonArray = new JSONArray();
        while (!parser.isClosed()) {
            JsonToken currentToken = parser.nextToken();

            if (currentToken == null) {
                break;
            }

            if (currentToken.equals(JsonToken.START_ARRAY)) {
                currentToken = parser.nextToken();
                while (!currentToken.equals(JsonToken.END_ARRAY)) {
                    switch (currentToken) {
                        case VALUE_NUMBER_INT:
                        case VALUE_NUMBER_FLOAT:
                            {
                                Number numberValue = parser.getNumberValue();
                                jsonArray.put(numberValue);
                                break;
                            }
                        case VALUE_FALSE:
                        case VALUE_TRUE:
                            {
                                boolean booleanValue = parser.getBooleanValue();
                                jsonArray.put(booleanValue);
                                break;
                            }
                        case VALUE_STRING:
                            {
                                String valueAsString = parser.getValueAsString();
                                jsonArray.put(valueAsString);
                                break;
                            }
                        case VALUE_EMBEDDED_OBJECT:
                            {
                                Object embeddedObject = parser.getEmbeddedObject();
                                jsonArray.put(embeddedObject);
                                break;
                            }
                        default:
                            break;
                    }
                    currentToken = parser.nextToken();
                }
            }
        }

        return jsonArray;
    }

    public <T> List<T> parseArray(String text, Class<T> clazz) {
        return null;
    }

    public List<Object> parseArray(String text, Type[] types) {
        return null;
    }

    public Optional<String> toJsonString(Object object) {
        try {
            return of(this.objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    public Optional<String> toJsonString(Object object, JsonFactory.Feature... features) {
        return configJsonFactoryAndReturnString(object, features);
    }

    public Optional<String> toJsonStringWithDateFormat(
            Object object, String dateFormat, JsonFactory.Feature... features) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        this.objectMapper.setDateFormat(simpleDateFormat);

        return configJsonFactoryAndReturnString(object, features);
    }

    private Optional<String> configJsonFactoryAndReturnString(
            Object object, JsonFactory.Feature[] features) {
        configJsonFactory(features);

        try {
            return of(this.objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    public Optional<String> toJsonString(
            Object object, FilterProvider filterProvider, JsonFactory.Feature... features) {
        this.objectMapper.setFilterProvider(filterProvider);
        return configJsonFactoryAndReturnString(object, features);
    }

    public Optional<byte[]> toJsonBytes(Object object, JsonFactory.Feature... features) {
        Optional<String> stringOptional = configJsonFactoryAndReturnString(object, features);
        return stringOptional.map(s -> s.getBytes(StandardCharsets.getDefaultCharset()));
    }

    public Optional<byte[]> toJsonBytes(
            Object object, FilterProvider filterProvider, JsonFactory.Feature... features) {
        this.objectMapper.setFilterProvider(filterProvider);
        return this.toJsonBytes(object, features);
    }

    public Optional<String> toJsonString(
            Object object, SerializationConfig config, JsonFactory.Feature... features) {
        this.objectMapper.setConfig(config);
        return configJsonFactoryAndReturnString(object, features);
    }

    public Optional<String> toJsonString(
            Object object,
            SerializationConfig config,
            FilterProvider filterProvider,
            JsonFactory.Feature... features) {
        this.objectMapper.setFilterProvider(filterProvider);
        return this.toJsonString(object, config, features);
    }

    public Optional<byte[]> toJsonBytes(
            Object object, SerializationConfig config, JsonFactory.Feature... features) {
        this.objectMapper.setConfig(config);
        return this.toJsonBytes(object, features);
    }

    public Optional<byte[]> toJsonBytes(
            Object object,
            SerializationConfig config,
            FilterProvider filterProvider,
            JsonFactory.Feature... features) {
        this.objectMapper.setFilterProvider(filterProvider);

        return toJsonBytes(object, config, features);
    }

    public Optional<String> toJsonString(Object object, boolean prettyFormat) {
        if (prettyFormat) {
            this.objectMapper.writerWithDefaultPrettyPrinter();
        }
        try {
            return of(this.objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    public void writeJsonString(Writer writer, Object object, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);
        this.objectMapper.writeValue(writer, object);
    }

    private void configJsonFactory(JsonFactory.Feature... features) {
        for (JsonFactory.Feature feature : features) {
            this.objectMapper.getFactory().enable(feature);
        }
    }

    public void writeJsonString(OutputStream os, Object object, JsonFactory.Feature... features)
            throws IOException {
        configJsonFactory(features);
        this.objectMapper.writeValue(os, object);
    }

    public Optional<Object> toJson(Object javaObject) {
        try {
            JSONObject jsonObject = new JSONObject();
            String jsonValue = this.objectMapper.writeValueAsString(javaObject);

            return of(jsonObject);
        } catch (JsonProcessingException e) {
            return empty();
        }
    }

    public Optional<Object> toJson(Object javaObject, ParserConfig parserConfig) {
        this.objectMapper.setConfig(parserConfig.getDeserializationConfig());
        return this.toJson(javaObject, parserConfig.getSerializationConfig());
    }

    public Optional<Object> toJson(Object javaObject, SerializationConfig config) {
        this.objectMapper.setConfig(config);
        try {
            String jsonValue = this.objectMapper.writeValueAsString(javaObject);
            JsonNode jsonNode = this.objectMapper.readTree(jsonValue);
            Map<String, Object> map = new HashMap<>();
            jsonNode.fieldNames()
                    .forEachRemaining(
                            fieldName -> {
                                JsonNode node = jsonNode.get(fieldName);
                                map.put(fieldName, node.toString());
                            });

            return of(new JSONObject(map));
        } catch (IOException e) {
            return empty();
        }
    }
}
