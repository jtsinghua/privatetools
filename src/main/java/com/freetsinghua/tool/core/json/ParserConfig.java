package com.freetsinghua.tool.core.json;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.freetsinghua.tool.util.JsonUtils;

/**
 * @author z.tsinghua
 * @date 2019/2/15
 */
public class ParserConfig {
    private final SerializationConfig serializationConfig;
    private final DeserializationConfig deserializationConfig;

    public ParserConfig() {
        this.serializationConfig = JsonUtils.getObjectMapper().getSerializationConfig();
        this.deserializationConfig = JsonUtils.getObjectMapper().getDeserializationConfig();
    }

    public SerializationConfig getSerializationConfig() {
        return serializationConfig;
    }

    public DeserializationConfig getDeserializationConfig() {
        return deserializationConfig;
    }
}
