package com.freetsinghua.tool.core.io;

import com.freetsinghua.tool.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/** create by @author z.tsinghua at 2018/9/14 */
public abstract class AbstractResource implements Resource {
    public AbstractResource() {}

    @Override
    public boolean exists() {
        try {
            return this.getFile().exists();
        } catch (IOException e) {
            try {
                InputStream inputStream = this.getInputStream();
                inputStream.close();
                return true;
            } catch (IOException e1) {
                return false;
            }
        }
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(this.getDescription() + " cannot be resolved to URL");
    }

    @Override
    public URI getUri() throws IOException {
        URL url = this.getURL();

        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException e) {
            throw new NestedIOException("Invalid URI[" + url + "]", e);
        }
    }

    @Override
    public File getFile() throws FileNotFoundException, IOException {
        throw new FileNotFoundException(
                getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return Channels.newChannel(this.getInputStream());
    }

    @Override
    public long contentLength() throws IOException {
        InputStream is = this.getInputStream();

        try {
            long size = 0L;

            int read;
            for (byte[] bytes = new byte[255]; (read = is.read(bytes)) != -1; size += read) {;
            }

            long result = size;

            return result;
        } finally {
            try {
                is.close();
            } catch (IOException e) {;
            }
        }
    }

    @Override
    public long lastModified() throws IOException {
        long lastModified = this.getFileForLastModifiedCheck().lastModified();

        if (0L == lastModified) {
            throw new FileNotFoundException(
                    this.getDescription()
                            + " cannot be resolved in the file system for resolve its last-modified timestamp");
        }

        return lastModified;
    }

    protected File getFileForLastModifiedCheck() throws IOException {
        return this.getFile();
    }

    @Override
    public Resource createRelative(String path) throws FileNotFoundException {
        throw new FileNotFoundException(
                "Cannot create a relative resource for " + this.getDescription());
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Resource && ((Resource) obj).getDescription().equals(this.getDescription());
    }
    
    @Override
    public int hashCode() {
        return this.getDescription().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getDescription();
    }
}
