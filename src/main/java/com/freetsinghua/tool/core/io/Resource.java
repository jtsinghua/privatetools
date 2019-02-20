package com.freetsinghua.tool.core.io;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/** create by @author z.tsinghua at 2018/9/14 */
public interface Resource extends InputStreamSource {
    /**
     * 资源是否存在
     *
     * @return 如果存在，返回{@code true}，否则返回{@code false}
     */
    boolean exists();

    /**
     * 是否可读
     *
     * @return 如果可读，返回{@code true}, 否则返回{@code false}
     */
    default boolean isReadable() {
        return true;
    }

    /** 文件是否已经打开 */
    default boolean isOpen() {
        return false;
    }

    /** 是否是一个文件资源 */
    default boolean isFile() {
        return false;
    }

    /** 返回{@link java.net.URL}
     * @throws IOException io异常
     * @return 返回结果
     * */
    URL getURL() throws IOException;

    URI getUri() throws IOException;

    File getFile() throws IOException;

    default ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(this.getInputStream());
    }

    long contentLength() throws IOException;

    long lastModified() throws IOException;

    Resource createRelative(String path) throws IOException;

    String getDescription();
}
