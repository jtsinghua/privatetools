package com.freetsinghua.tool.core.io;

import java.io.IOException;
import java.io.InputStream;

/** create by @author z.tsinghua at 2018/9/14 */
public interface InputStreamSource {

    InputStream getInputStream() throws IOException;
}
