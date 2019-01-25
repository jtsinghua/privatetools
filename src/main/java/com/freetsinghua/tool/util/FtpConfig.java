package com.freetsinghua.tool.util;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * FTP的配置类
 */
@Setter
@Getter
final class FtpConfig {
    private String host;
    private int port;
    private String username;
    private String password;

    public void setPassword(String password) {
        // 解码
        this.password = new String(Base64.getDecoder().decode(password), StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return JsonUtils.writeObjectAsString(this);
    }
}
