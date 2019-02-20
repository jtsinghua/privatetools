package com.freetsinghua.tool.core.io;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

import lombok.extern.slf4j.Slf4j;

/** create by @author z.tsinghua at 2018/9/14 */
@Slf4j
public abstract class AbstractFileResolvingResource extends AbstractResource {

    public AbstractFileResolvingResource() {}

    @Override
    public boolean exists() {
        try {
            URL url = getURL();

            if (ResourceUtils.isFileUrl(url)) {
                return this.getFile().exists();
            } else {
                URLConnection urlConnection = url.openConnection();
                this.customizeConnection(urlConnection);
                HttpURLConnection conn =
                        urlConnection instanceof HttpURLConnection
                                ? (HttpURLConnection) urlConnection
                                : null;
                if (null != conn) {
                    int code = conn.getResponseCode();
                    if (CommonConstant.HTTP_RESPONSE_OK == code) {
                        return true;
                    }
                    if (CommonConstant.HTTP_RESPONSE_ERROR == code) {
                        return false;
                    }
                }

                if (urlConnection.getContentLength() > 0) {
                    return true;
                } else if (null != conn) {
                    conn.disconnect();
                    return false;
                } else {
                    InputStream is = this.getInputStream();
                    is.close();
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        try {
            URL url = this.getURL();

            if (ResourceUtils.isFileUrl(url)) {
                return true;
            } else {
                File file = this.getFile();
                return file.canRead() && !file.isDirectory();
            }

        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isFile() {
        try {
            return "file".equals(this.getURL().getProtocol());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public File getFile() throws IOException {
        URL url;
        url = this.getURL();
        if (log.isDebugEnabled()) {
            log.debug("getFile: url = [" + url + "]");
        }
        return ResourceUtils.getFile(url, this.getDescription());
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        try {
            return FileChannel.open(this.getFile().toPath(), StandardOpenOption.READ);
        } catch (NoSuchFileException | FileNotFoundException e) {
            return super.readableChannel();
        }
    }

    @Override
    public long contentLength() throws IOException {
        URL url = this.getURL();

        if (ResourceUtils.isFileUrl(url)) {
            return this.getFile().length();
        } else {
            URLConnection urlConnection = url.openConnection();
            this.customizeConnection(urlConnection);
            return urlConnection.getContentLengthLong();
        }
    }

    @Override
    public long lastModified() throws IOException {
        URL url = this.getURL();

        if (ResourceUtils.isFileUrl(url) || ResourceUtils.isJarFile(url)) {
            try {

                return super.lastModified();
            } catch (FileNotFoundException e) {;
            }
        }

        URLConnection conn = url.openConnection();
        this.customizeConnection(conn);
        return conn.getLastModified();
    }

    protected void customizeConnection(URLConnection con) throws IOException {
        ResourceUtils.useCachesIfNecessary(con);

        if (con instanceof HttpURLConnection) {
            this.customizeConnection((HttpURLConnection) con);
        }
    }

    protected void customizeConnection(HttpURLConnection con) throws IOException {
        con.setRequestMethod("HEAD");
    }

    @Override
    protected File getFileForLastModifiedCheck() throws IOException {
        URL url = this.getURL();
        if (ResourceUtils.isJarFile(url)) {
            URL actualUrl = ResourceUtils.extractArchiveURL(url);
            return ResourceUtils.getFile(actualUrl, "Jar URL");
        } else {
            return this.getFile();
        }
    }

    @Nullable
    public abstract String getFilename();
}
