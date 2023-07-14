package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.exception.DkException;
import io.netty.buffer.ByteBuf;

/**
 * @author tangbu
 */
public class JsonObjectEncoder {

    ObjectMapper mapper = new ObjectMapper();
    public byte[] OBJECT_LENGTH_PLACE_HOLDER = new byte[4];

    public void encode(JsonNode jsonObject, ByteBuf out) throws DkException {
        try {
            out.writeBytes(OBJECT_LENGTH_PLACE_HOLDER);
            byte[] jsonBytes = mapper.writeValueAsBytes(jsonObject);
            out.writeBytes(jsonBytes);
            out.setInt(out.writerIndex() - jsonBytes.length - OBJECT_LENGTH_PLACE_HOLDER.length, jsonBytes.length);
        } catch (JsonProcessingException e) {
            throw new DkException(e);
        }
    }

}
