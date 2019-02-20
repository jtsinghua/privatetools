package com.freetsinghua.tool.core.io;

import java.io.IOException;
import java.io.InputStream;

/** create by @author z.tsinghua at 2018/9/14 */
public interface InputStreamSource {
    /**
     * 获取{@link InputStream}
     *
     * @return 返回输入流
     * @throws IOException 若是在获取的过程中发生io错误，则抛出异常
     */
    InputStream getInputStream() throws IOException;
}
