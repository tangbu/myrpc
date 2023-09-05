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


    public void encode(JsonNode jsonObject, ByteBuf out) throws DkException {
        try {
            byte[] jsonBytes = mapper.writeValueAsBytes(jsonObject);
            out.writeBytes(jsonBytes);
        } catch (JsonProcessingException e) {
            throw new DkException(e);
        }
    }

}
