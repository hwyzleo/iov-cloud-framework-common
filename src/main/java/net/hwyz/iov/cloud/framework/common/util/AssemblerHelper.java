package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Map;

/**
 * 装配辅助类
 *
 * @author hwyz_leo
 */
public class AssemblerHelper {

    /**
     * JSON转Map
     *
     * @param json JSON
     * @return Map
     */
    public static Map<String, Object> json2Map(String json) {
        return JSONUtil.toBean(json, new TypeReference<Map<String, Object>>() {
        }, true);
    }

    /**
     * Map转JSON
     *
     * @param map Map
     * @return JSON
     */
    public static String map2Json(Map<String, Object> map) {
        return JSONUtil.toJsonStr(map);
    }

    /**
     * JSON字符串转JSON对象
     *
     * @param jsonStr JSON字符串
     * @return JSON对象
     */
    public static JSONObject string2Json(String jsonStr) {
        return JSONUtil.parseObj(jsonStr);
    }

    /**
     * JSON对象转JSON字符串
     *
     * @param json JSON对象
     * @return JSON字符串
     */
    public static String json2String(JSONObject json) {
        return json.toString();
    }

}
