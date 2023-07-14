package com.ndkey.demo.overview.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndkey.exception.DkException;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * @author tangbu
 */
public class JsonObjectDecoder {
    ObjectMapper mapper = new ObjectMapper();
    public byte[] OBJECT_LENGTH_PLACE_HOLDER = new byte[4];

    public JsonNode decode(ByteBuf in) throws DkException {
        int length = in.readInt();
        byte[] bodyBytes = new byte[length];
        in.readBytes(bodyBytes);
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readValue(bodyBytes, JsonNode.class);
        } catch (IOException e) {
            throw new DkException(e);
        }

        return jsonNode;
    }
}
