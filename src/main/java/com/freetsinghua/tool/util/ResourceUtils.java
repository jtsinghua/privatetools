package com.freetsinghua.tool.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;

/** create by @author z.tsinghua at 2018/9/14 */
public class ResourceUtils {

    private static final String PROTOCOL_FILE = "file";
    private static final String PROTOCOL_JAR = "jar";
    private static final String PROTOCOL_ZIP = "zip";
    private static final String PROTOCOL_WAR = "war";

    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    public static boolean isFileUrl(URL url) {
        String protocol = url.getProtocol();

        return PROTOCOL_FILE.equals(protocol);
    }

    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        assertNotNull(resourceUrl, "Resource URL must not be null");

        if (!PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                    description
                            + " cannot be resolved to absolute file path because it does not reside in the file system: "
                            + resourceUrl);
        }

        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            return new File(resourceUrl.getFile());
        }
    }

    public static void assertNotNull(Object object, String message) {
        if (null == object) {
            throw new NullPointerException(message);
        }
    }
    
    public static boolean isJarFile(URL url){
        String protocol = url.getProtocol();
        
        return PROTOCOL_JAR.equals(protocol) || PROTOCOL_ZIP.equals(protocol) || PROTOCOL_WAR.equals(protocol);
    }
    
    public static void useCachesIfNecessary(URLConnection connection){
        connection.setUseCaches(connection.getClass().getSimpleName().startsWith("JNLP"));
    }
    
    public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int endIndex = urlFile.indexOf("*/");
        if (-1 != endIndex){
            String warFile = urlFile.substring(0, endIndex);
            if (PROTOCOL_WAR.equals(jarUrl.getProtocol())){
                return new URL(warFile);
            }
            
            int startIndex = warFile.indexOf("war:");
            if (-1 != startIndex){
                return new URL(warFile.substring(startIndex + "war:".length()));
            }
        }
        
        return extractJarFileURL(jarUrl);
    }
    
    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
        String jarUrlFile = jarUrl.getFile();
    
        int index = jarUrlFile.indexOf("!/");
        if (-1 != index){
            String jarFile = jarUrlFile.substring(0, index);
    
            try {
                return new URL(jarFile);
            } catch (MalformedURLException e) {
                if (!jarFile.startsWith("/")){
                    jarFile = "/" + jarFile;
                }
                
                return new URL("file:" + jarFile);
            }
        }else {}
        return jarUrl;
    }
}
