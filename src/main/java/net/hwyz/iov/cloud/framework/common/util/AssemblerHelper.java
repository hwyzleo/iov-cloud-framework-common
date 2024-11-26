package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.lang.TypeReference;
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

}
