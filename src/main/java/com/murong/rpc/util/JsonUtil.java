package com.murong.rpc.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    public static <T> T parseObject(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String objcont) {
            return JSON.parseObject(objcont, clazz);
        }
        return JSON.parseObject(JSON.toJSONString(obj), clazz);
    }

    public static <T> List<T> parseArray(Collection collection, Class<T> clazz) {
        if (collection == null) {
            return null;
        }
        return parseArray(JSON.toJSONString(collection), clazz);
    }

    public static <T> List<T> parseArray(String jsonArry, Class<T> clazz) {
        if (jsonArry == null) {
            return null;
        }
        return JSONArray.parseArray(jsonArry, clazz);
    }

    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }
}
