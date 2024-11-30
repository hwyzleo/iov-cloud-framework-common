package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.util.ArrayUtil;
import net.hwyz.iov.cloud.framework.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * 字符串工具类
 *
 * @author hwyz_leo
 */
public class StrUtil extends cn.hutool.core.util.StrUtil {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * 截取字符串
     *
     * @param str              字符串
     * @param fromIndexInclude 开始的index（包括）
     * @param toIndexExclude   结束的index（不包括）
     * @return 结果
     */
    public static String substring(CharSequence str, int fromIndexInclude, int toIndexExclude) {
        return cn.hutool.core.util.StrUtil.sub(str, fromIndexInclude, toIndexExclude);
    }

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean isHttp(String link) {
        return cn.hutool.core.util.StrUtil.startWithAnyIgnoreCase(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 判断给定的collection列表中是否包含数组array 判断给定的数组array中是否包含给定的元素value
     *
     * @param collection 给定的集合
     * @param array      给定的数组
     * @return boolean 结果
     */
    public static boolean containsAny(Collection<String> collection, String... array) {
        if (ArrayUtil.isEmpty(collection) || ArrayUtil.isEmpty(array)) {
            return false;
        } else {
            for (String str : array) {
                if (collection.contains(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return 结果
     */
    public static String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * 依次替换
     *
     * @param text            原始文本
     * @param searchList      查询列表
     * @param replacementList 替换列表
     * @return 结果
     */
    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        return StringUtils.replaceEach(text, searchList, replacementList);
    }

}
