package net.hwyz.iov.cloud.framework.common.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 客户端语言枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum ClientLanguage {

    ZH("中文", "zh"),
    EN("英语", "en");

    public final String label;

    public final String code;

    public static ClientLanguage valOf(String code) {
        return Arrays.stream(ClientLanguage.values())
                .filter(clientLanguage -> clientLanguage.code.equals(code))
                .findFirst()
                .orElse(ZH);
    }

}
