package com.app.appplatform.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .build();

    /**
     * 函数式接口，用于执行JSON操作
     */
    @FunctionalInterface
    private interface JsonOperation<T> {
        T execute() throws JsonProcessingException;
    }

    /**
     * 执行JSON操作并处理异常
     */
    private static <T> T executeJsonOperation(JsonOperation<T> operation, String errorMessage) {
        try {
            return operation.execute();
        } catch (JsonProcessingException e) {
            log.error("{}: {}", errorMessage, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通用的JSON解析方法
     *
     * @param json  JSON字符串
     * @param parser 解析器函数
     * @param <T>   返回类型
     * @return 解析后的对象，解析失败返回null
     */
    private static <T> T parseJson(String json, JsonParser<T> parser) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            // 尝试直接解析
            return parser.parse(json);
        } catch (Exception e) {
            try {
                // 如果直接解析失败，尝试清理字符串后重试
                String cleanedJson = cleanJsonString(json);
                return parser.parse(cleanedJson);
            } catch (Exception ex) {
                log.error("JSON转换失败: {}", ex.getMessage());
                return null;
            }
        }
    }

    private static String cleanJsonString(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        String cleaned = json.trim();

        // 处理类似 UUID 的值（包含连字符的字符串）
        cleaned = cleaned.replaceAll(
                "([{,]\\s*[\\w$]+\\s*:\\s*)([0-9a-zA-Z][0-9a-zA-Z.-]*[0-9a-zA-Z])([,\\}])",
                "$1\"$2\"$3"
        );

        // 处理不带引号的键
        cleaned = cleaned.replaceAll(
                "([{,]\\s*)([a-zA-Z_$][a-zA-Z_$0-9]*)(\\s*:)",
                "$1\"$2\"$3"
        );

        // 处理不带引号的字符串值
        cleaned = cleaned.replaceAll(
                "(:\\s*)([a-zA-Z_$][a-zA-Z_$0-9.-]*)([,\\}])",
                "$1\"$2\"$3"
        );

        // 确保字符串以 { 或 [ 开头
        if (!cleaned.startsWith("{") && !cleaned.startsWith("[")) {
            if (cleaned.startsWith("\"")) {
                cleaned = cleaned.substring(1);
            }
            cleaned = "{" + cleaned;
        }

        // 确保字符串以 } 或 ] 结尾
        if (!cleaned.endsWith("}") && !cleaned.endsWith("]")) {
            if (cleaned.endsWith("\"")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            cleaned = cleaned + "}";
        }

        return cleaned;
    }
    
    /**
     * 函数式接口，用于JSON解析
     */
    @FunctionalInterface
    private interface JsonParser<T> {
        T parse(String json) throws JsonProcessingException;
    }
    
    /**
     * 将JSON字符串转换为Java对象
     * 自动处理 key 和 value 不带引号的情况
     *
     * @param json  JSON字符串（支持不带引号的key和value）
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 转换后的Java对象，转换失败时返回null
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return parseJson(json, jsonStr -> objectMapper.readValue(jsonStr, clazz));
    }

    /**
     * 将JSON字符串转换为复杂类型对象（如List、Map等）
     * 自动处理 key 和 value 不带引号的情况
     *
     * @param json    JSON字符串（支持不带引号的key和value）
     * @param typeRef 目标类型引用，例如：new TypeReference<List<YourClass>>>(){}
     * @param <T>     泛型类型
     * @return 转换后的Java对象，转换失败时返回null
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        return parseJson(json, jsonStr -> objectMapper.readValue(jsonStr, typeReference));
    }

    /**
     * 将Java对象转换为JSON字符串
     *
     * @param obj 要转换的Java对象
     * @return JSON字符串，转换失败时返回null
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Java对象转换为格式化的JSON字符串（美化输出）
     *
     * @param obj 要转换的Java对象
     * @return 格式化后的JSON字符串，转换失败时返回null
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return executeJsonOperation(
            () -> objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj),
            "对象转JSON(格式化)失败"
        );
    }

    /**
     * 压缩JSON字符串，去除空格、换行符等多余字符
     *
     * @param json JSON字符串
     * @return 压缩后的JSON字符串，处理失败返回原字符串
     */
    public static String minify(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON压缩失败，返回原内容: {}", e.getMessage());
            return json;
        }
    }
}
