package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 国家或地区枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum CountryRegion {

    /** 中国大陆 **/
    CHINESE_MAINLAND("中国大陆", "+86"),
    /** 中国香港 **/
    CHINESE_HONG_KONG("中国香港", "+852"),
    /** 中国澳门 **/
    CHINESE_MACAU("中国澳门", "+853"),
    /** 中国台湾 **/
    CHINESE_TAIWAN("中国台湾", "+886");

    public final String name;
    public final String code;

    /**
     * 获取对应的国家或地区枚举类
     *
     * @param code 国家或地区代码
     * @return 国家或地区枚举类
     */
    public static CountryRegion valOf(String code) {
        return Arrays.stream(CountryRegion.values())
                .filter(countryRegion -> countryRegion.code.equals(code))
                .findFirst()
                .orElse(CHINESE_MAINLAND);
    }

}
