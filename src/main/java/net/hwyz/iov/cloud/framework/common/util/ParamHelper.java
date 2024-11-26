package net.hwyz.iov.cloud.framework.common.util;

import cn.hutool.core.util.StrUtil;
import net.hwyz.iov.cloud.framework.common.bean.ClientAccount;
import net.hwyz.iov.cloud.framework.common.bean.MptAccount;

/**
 * 参数辅助类
 *
 * @author hwyz_leo
 */
public class ParamHelper {

    /**
     * 获取客户端账号信息
     *
     * @param clientAccount 客户端账号信息
     * @return 客户端账号信息
     */
    public static String getClientAccountInfo(ClientAccount clientAccount) {
        if (clientAccount == null) {
            return null;
        }
        return StrUtil.emptyIfNull(clientAccount.getClientId()) + ":" + StrUtil.emptyIfNull(clientAccount.getAccountId());
    }

    /**
     * 获取后台管理用户账号信息
     *
     * @param mptAccount 后台管理用户账号
     * @return 后台管理用户账号信息
     */
    public static String getMptAccountInfo(MptAccount mptAccount) {
        if (mptAccount == null) {
            return null;
        }
        return StrUtil.emptyIfNull(mptAccount.getUsername()) + ":" + StrUtil.emptyIfNull(mptAccount.getAccountId());
    }

}
