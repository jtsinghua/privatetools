package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import lombok.extern.slf4j.Slf4j;

/** create by @author z.tsinghua at 2018/9/14 */
@Slf4j
public class FileUtils {

    private static final String EMPTY_STRING = "";

    /**
     * 写入文件
     *
     * @param content 要写入文件的内容
     * @param file 文件
     * @throws IOException io错误
     */
    public static void writeCharSequenceToFile(CharSequence content, File file) throws IOException {
        Assert.state(StringUtils.hasLength(content), "Content must not be null");
        Assert.state(file != null, "File must not be null");
        BufferedWriter writer =
                new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        writer.append(content);
        writer.flush();
        writer.close();
    }

    /**
     * read file to string
     *
     * @param path the file path
     * @return if file exist, return the content of this file
     */
    public static String readFileToString(String path) {
        try {
            StringBuilder buf = new StringBuilder();

            File file = new File(path);
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(file),
                                    StandardCharsets.getDefaultCharset()));
            String line;
            String lineSeparator = SystemUtils.getLineSeparator();
            if (lineSeparator == null) {
                lineSeparator = "\n";
            }

            while ((line = reader.readLine()) != null) {
                buf.append(line).append(lineSeparator);
            }

            return buf.toString();
        } catch (IOException e) {
            log.error("Can't read file: {}", e.getMessage(), e);
        }
        return EMPTY_STRING;
    }

    /**
     * Get the temp directory path
     *
     * @return the temp directory path
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Get the current user's home path
     *
     * @return home path
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Get the current user's home directory
     *
     * @return home directory
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    /**
     * Construct a file from the set of name elements. for example: E:/ demos test.txt it will
     * return E:/demos/test.txt
     *
     * @param directory the parent directory
     * @param names the name elements
     * @return file, at client, should use file.exists()
     */
    public static File getFile(final File directory, String... names) {
        if (null == directory) {
            throw new NullPointerException("directory must not be null.");
        }

        if (null == names) {
            throw new NullPointerException("names must not be null");
        }

        File file = directory;

        for (String name : names) {
            file = new File(file, name);
        }

        return file;
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param names the name elements
     * @return the file, it can be null
     */
    @Nullable
    public static File getFile(String... names) {
        if (null == names) {
            throw new NullPointerException("names must not be null");
        }

        File file = null;

        for (String name : names) {
            if (null == file) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }

        return file;
    }

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better * error messages
     * than simply calling <code>new FileInputStream(file)</code>.
     *
     * @param file the file to open for input, must not be {@code null}
     * @return a new {@link FileInputStream} for the specified file
     * @throws IOException if the file is directory
     * @throws IOException if the file cannot be read
     * @throws NullPointerException if the file is null
     */
    public static FileInputStream openInputStream(final File file) throws IOException {
        if (null == file) {
            throw new NullPointerException("file must not be null");
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException(
                        "File [" + file.getName() + "] is exists but it is directory");
            }

            if (!file.canRead()) {
                throw new IOException("File [" + file.getName() + "] cannot be read.");
            }
        } else {
            throw new FileNotFoundException("File [" + file.getName() + "] does not exists.");
        }

        return new FileInputStream(file);
    }
}
