package com.fast.gateway.common.util;

import com.fast.gateway.common.constants.BasicConst;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * @author sheng
 * @create 2023-07-03 17:37
 */
public class JSONUtil {

    public static final String CODE = "code";

    public static final String STATUS = "status";

    public static final String DATA = "data";

    public static final String MESSAGE = "message";

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final JsonFactory jsonFactory = mapper.getFactory();

    static {
        mapper.setDateFormat(new SimpleDateFormat(BasicConst.DATE_FORMAT));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        mapper.addMixIn(Object.class, ExcludeFilter.class);
        mapper.setFilterProvider(new SimpleFilterProvider().addFilter("excludeFilter", SimpleBeanPropertyFilter.serializeAllExcept("class")));
    }

    public static String toJSONString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("object format to json error: " + obj, e);
        }
    }

    public static void outputToWriter(Writer out, Object value) {
        try {
            mapper.writeValue(out, value);
        } catch (IOException e) {
            throw new RuntimeException("output to writer error: " + value, e);
        }
    }

    public static <T> T parse(JsonNode body, Class<T> clz) {
        try {
            return mapper.readValue(body.traverse(), clz);
        } catch (Exception e) {
            throw new RuntimeException("json node parse to object [" + clz + "] error:" + body, e);
        }
    }

    public static <T> T parse(String str, Class<T> clz) {
        try {
            return mapper.readValue(str == null ? "{}": str, clz);
        } catch (Exception e) {
            throw new RuntimeException("json parse to object [" + clz + "] error:" + str, e);
        }
    }

    public static <T> T parse(Optional<String> json, Class<T> clz) {
        return json.map((str) -> parse(str, clz)).orElse(null);
    }

    public static <T> T parse(String str, TypeReference<T> tr) {
        try {
            return mapper.readValue(str, tr);
        } catch (Exception e) {
            throw new RuntimeException("json parse to object [" + tr + "] error:" + str, e);
        }
    }

    public static <T> T parse(JsonNode body, JavaType javaType) {
        try {
            return mapper.readValue(body.traverse(), javaType);
        } catch (IOException e) {
            throw new RuntimeException("json parse to object [" + body + "] error:" + body, e);
        }
    }

    public static <T> T parse(String str, JavaType javaType) {
        try {
            return mapper.readValue(str, javaType);
        } catch (Exception e) {
            throw new RuntimeException("json parse to object [" + str + "] error:" + str, e);
        }
    }

    public static <T> List<T> parseToList(String json, Class<T> clz) {
        return parse(json, getCollectionType(List.class, clz));
    }

    public static JsonNode tree(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("object format to json error: " + json, e);
        }
    }

    public static String serializeAllExcept(Object obj, String... filterFields) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            FilterProvider filters = new SimpleFilterProvider().addFilter(obj.getClass().getName(),
                    SimpleBeanPropertyFilter.serializeAllExcept(filterFields));
            mapper.setFilterProvider(filters).setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                private static final long serialVersionUID = -5946508772105624249L;

                @Override
                public Object findFilterId(Annotated ac) {
                    return ac.getName();
                }
            });
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("object format to json error:" + obj, e);
        }
    }

    public static String filterOutAllExcept(Object obj, String... filterFields) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            FilterProvider filters = new SimpleFilterProvider().addFilter(obj.getClass().getName(),
                    SimpleBeanPropertyFilter.filterOutAllExcept(filterFields));
            mapper.setFilterProvider(filters).setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                private static final long serialVersionUID = 8992650895517982839L;

                @Override
                public Object findFilterId(Annotated ac) {
                    return ac.getName();
                }
            });
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("object format to json error:" + obj, e);
        }
    }

    public static String parseOneField(String str, String fieldName) {
        try {
            JsonParser jsonParser = jsonFactory.createParser(str);
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String currentFieldName = jsonParser.getCurrentName();
                if (fieldName.equals(currentFieldName)) {
                    jsonParser.nextToken();
                    return jsonParser.getText();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("object format to json error: ", e);
        }
        return null;
    }

    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    @JsonFilter("excludeFilter")
    public static class ExcludeFilter {

    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
