package com.freetsinghua.tool.util;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;

/** FTP的配置类 */
@Setter
@Getter
final class FtpConfig {
    private String host;
    private int port;
    private String username;
    private String password;

    public void setPassword(String password) {
        // 解码
        this.password = SafeUtils.decode(password);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
