package com.hrlee.transnaviserver.springboot.service.route.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class ListReverseJsonSerializer extends JsonSerializer<List<?>> {

    @Override
    public void serialize(List<?> targetList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for(int i=targetList.size()-1; i>=0; i--)
            jsonGenerator.writeObject(targetList.get(i));
        jsonGenerator.writeEndArray();
    }
}
