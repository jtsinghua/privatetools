package com.freetsinghua.tool.util;

import com.freetsinghua.tool.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** create by @author z.tsinghua at 2018/9/14 */
public class FileUtils {

    private static final String EMPTY_STRING = "";
    
    /**
     * read file to string
     *
     * @param path the file path
     * @return if file exist, return the content of this file
     * @throws IOException if file does not exist
     */
    public static String readFileToString(String path) {

        BufferedReader reader = null;

        try {
            ClassPathResource resource = new ClassPathResource(path);

            reader =
                    new BufferedReader(
                            new InputStreamReader(new FileInputStream(resource.getFile())));

            StringBuilder builder = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            return builder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return EMPTY_STRING;
    }
}
