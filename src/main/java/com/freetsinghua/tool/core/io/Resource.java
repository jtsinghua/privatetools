package com.freetsinghua.tool.core.io;

import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * create by @author z.tsinghua at 2018/9/14
 */
public interface Resource extends InputStreamSource{
    
    boolean exists();
    
    default boolean isReadable(){
        return true;
    }
    
    default boolean isOpen(){
        return false;
    }
    
    default boolean isFile(){
        return false;
    }
    
    URL getUrl() throws IOException;
    
    URI getUri() throws IOException;
    
    File getFile() throws IOException;
    
    default ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(this.getInputStream());
    }
    
    long contentLength() throws IOException;
    
    long lastModified() throws IOException;
    
    Resource createRelative(String path) throws IOException;
    
    @Nullable
    String getFileName();
    
    String getDescription();
}
