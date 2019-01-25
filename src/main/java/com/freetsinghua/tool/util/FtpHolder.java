package com.freetsinghua.tool.util;

import com.freetsinghua.tool.core.io.ClassPathResource;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * FTP客户端
 *
 * <p>在ftpconfig.properties文件中配置相关信息
 *
 * @author z.tsinghua
 * @date 2019/1/24
 */
@Slf4j
public final class FtpHolder {
    private FtpConfig ftpConfig = new FtpConfig();
    private FTPClient ftpClient = null;
    private static ThreadLocal<FtpHolder> ftpHolderThreadLocal = new ThreadLocal<>();

    private FtpHolder() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("ftpconfig.properties");
            Properties properties = new Properties();
            // 加载配置文件
            properties.load(classPathResource.getInputStream());
            // 获取FTP属性
            ftpConfig.setHost(properties.getProperty("ftp.server.host", "localhost"));
            ftpConfig.setPort(Integer.parseInt(properties.getProperty("ftp.server.port", "21")));
            ftpConfig.setUsername(properties.getProperty("ftp.user.name", "admin"));
            ftpConfig.setPassword(properties.getProperty("ftp.user.password", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FtpHolder getInstance() {
        FtpHolder ftpHolder = ftpHolderThreadLocal.get();
        if (Objects.isNull(ftpHolder)) {
            ftpHolder = new FtpHolder();
            ftpHolderThreadLocal.remove();
            ftpHolderThreadLocal.set(ftpHolder);
        }
        return ftpHolder;
    }

    /**
     * 断开于服务器的连接
     *
     * @throws IOException 当断开连接时发生错误，则抛出io异常
     */
    private void disconnect() throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    /**
     * 建立与服务器的连接
     *
     * @return 返回结果
     * @throws IOException 若是有io异常，则抛出
     */
    private boolean connect() throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(ftpConfig.getHost(), ftpConfig.getPort());
        ftpClient.login(ftpConfig.getUsername(), ftpConfig.getPassword());
        // 验证是否登录成功
        int replyCode = ftpClient.getReplyCode();
        if (FTPReply.isPositiveCompletion(replyCode)) {
            log.info("connect: 连接服务器成功");
            return true;
        }
        log.error("connect: 服务器连接失败, 错误码: [{}]", replyCode);
        return false;
    }

    /**
     * 上传文件到服务器
     *
     * @param serverPath 要将文件上传到具体的哪个目录
     * @param fileName   文件名
     * @param localPath  本地文件的绝对路径
     * @return 返回true，表示上传成功；false，表示上传失败
     */
    public boolean uploadFileToFtpServer(String serverPath, String fileName, String localPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(localPath);
            boolean isConnectSuccess = connect();
            if (!isConnectSuccess) {
                throw new IllegalAccessException("无法连接服务器，请确认服务器是否启动，或者检查用户名和密码是否正确");
            }
            // 创建目录，并将工作目录切换成创建成功的目录
            boolean isCreateDirectorySuccess = createDirectory(serverPath);
            if (!isCreateDirectorySuccess) {
                throw new RuntimeException("uploadFileToFtpServer: 创建目录失败");
            }
            // 保存文件
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.storeFile(fileName, fileInputStream);
            fileInputStream.close();
            // 退出登录
            ftpClient.logout();
            // 断开连接
            disconnect();
            // 若一切顺利
            return true;
        } catch (IllegalAccessException e) {
            log.error("uploadFileToFtpServer: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error("uploadFileToFtpServer: 指定文件不存在: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("uploadFileToFtpServer: io错误: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param serverPath 在远程服务器上，文件所在的目录
     * @param fileName   所要下载的文件的名称
     * @param localPath  本地保存路径
     * @return 返回操作结果
     * @throws IllegalAccessException 若是指定的目录不存在，则抛出异常
     * @throws FileNotFoundException  若是指定要下载的文件不存在，则抛出异常
     */
    public boolean downloadFileFromFtpServer(String serverPath, String fileName, String localPath)
            throws IllegalAccessException, IOException {
        // 先校验目录是否存在
        if (connect() && !isFileOrPathExist(serverPath)) {
            throw new IllegalAccessException("路径[" + serverPath + "]不存在，请确认");
        }
        // 如果路径存在，校验文件是否存在
        if (!isFileOrPathExist(serverPath + "/" + fileName)) {
            throw new FileNotFoundException("文件[" + fileName + "]不存在");
        }
        // 文件存在，则下载
        // 处理本地文件路径
        if (!localPath.endsWith("/")) {
            localPath += "/";
        }
        // 切换工作目录
        ftpClient.changeWorkingDirectory(serverPath);
        // 获取当前工作目录下所有的文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.getName().equalsIgnoreCase(fileName)) {
                // 找到要下载的文件
                File localFile = new File(localPath + fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                ftpClient.retrieveFile(fileName, fileOutputStream);
                fileOutputStream.close();
                log.info("downloadFileFromFtpServer: 下载文件成功");
                return true;
            }
        }
        log.error("downloadFileFromFtpServer: 下载文件失败");
        return false;
    }

    /**
     * 从FTP服务器上删除文件
     *
     * @param serverPath 要删除文件所在的目录
     * @param fileName   要删除的文件的名称
     * @return 返回操作结果
     */
    public boolean deleteFileFromFtpServer(String serverPath, String fileName)
            throws IOException, IllegalAccessException {
        // 校验路径是否存在
        if (connect() && !isFileOrPathExist(serverPath)) {
            throw new IllegalAccessException("目录[" + serverPath + "]不存在");
        }

        if (!serverPath.equalsIgnoreCase("/") && !serverPath.endsWith("/")) {
            serverPath += "/";
        }
        // 校验指定的文件是否存在
        if (!isFileOrPathExist(serverPath + fileName)) {
            throw new FileNotFoundException("指定的文件[" + fileName + "]不存在");
        }
        // 切换目录
        ftpClient.changeWorkingDirectory(serverPath);
        boolean deleteFileSuccess = ftpClient.deleteFile(fileName);
        if (deleteFileSuccess) {
            log.info("deleteFileFromFtpServer: 删除文件成功");
            FTPFile[] ftpFiles = ftpClient.listFiles();
            // 若是空目录，则移除
            if (ftpFiles.length == 0) {
                // 截取要删除的目录名称
                String[] strings = serverPath.split("/");
                if (strings.length == 0) {
                    // 说明是根目录,直接退出，不再处理
                    return true;
                }

                List<String> directories = Lists.newArrayList(strings);
                while (!directories.isEmpty()) {
                    // 工作目录切换到父目录
                    ftpClient.changeToParentDirectory();
                    FTPFile[] listDirectories = ftpClient.listDirectories();
                    for (FTPFile ftpFile : listDirectories) {
                        if (directories.size() < 1) {
                            break;
                        }

                        String directoryName = directories.get(directories.size() - 1);
                        if (directoryName.trim().length() != 0
                                && ftpFile.isDirectory()
                                && ftpFile.getName().equalsIgnoreCase(directoryName)) {
                            ftpClient.removeDirectory(ftpFile.getName());
                            break;
                        }
                    }
                    directories.remove(directories.size() - 1);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 在服务器创建目录
     *
     * @param remotePath 要创建的目录结构 如/test/
     * @return 返回操作结果
     */
    private boolean createDirectory(String remotePath) throws IOException {
        if (!remotePath.startsWith("/")) {
            throw new RuntimeException("路径必须以/开头");
        }

        // 如果文件要存在用户目录的根目录下
        if (remotePath.equalsIgnoreCase("/")) {
            return true;
        }

        return makeDirectory(remotePath);
    }

    /**
     * 检查文件或者目录是否存在是否存在
     *
     * <p>检查目录时，如/test, /test/
     *
     * <p>检查文件时,如/test/test.txt
     *
     * @param pathname The file or directory to list
     * @return 若是存在，返回true
     */
    public boolean isFileOrPathExist(String pathname) {
        try {
            return ftpClient.listFiles(pathname).length > 0;
        } catch (IOException e) {
            log.error("isFileOrPathExist: 出现错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 在FTP服务器创建目录
     *
     * @param dir 待创建的目录
     * @return 返回操作结果，true，若是创建成功
     * @throws IOException 若是在操作过程中出现错误，则抛出异常
     */
    private boolean makeDirectory(String dir) throws IOException {
        // 分割路径
        String[] paths = dir.split("/");
        StringBuilder currentPath = new StringBuilder("/");
        for (String path : paths) {
            if (StringUtils.isBlank(path.trim())) {
                continue;
            }
            // 进入目录
            currentPath.append(path).append("/");
            if (isFileOrPathExist(currentPath.toString())) {
                // 若是目录已经存在，则进入
                ftpClient.changeWorkingDirectory(currentPath.toString());
            } else {
                // 若是不存在，则创建
                ftpClient.makeDirectory(currentPath.toString());
            }
        }
        // 切换工作目录
        ftpClient.changeWorkingDirectory(currentPath.toString());
        return true;
    }

    public static void main(String[] args) throws IOException, IllegalAccessException {
        // download();
//         upload();
         delete();
    }

    @SuppressWarnings("unused")
    private static void delete() throws IOException, IllegalAccessException {
        boolean deleteFileFromFtpServer =
                FtpHolder.getInstance().deleteFileFromFtpServer("/test/test", "uuids.txt");
        if (deleteFileFromFtpServer) {
            log.info("删除成功");
        } else {
            log.error("删除失败");
        }
    }

    @SuppressWarnings("unused")
    private static void download() throws IOException, IllegalAccessException {
        boolean downloadFileFromFtpServer =
                FtpHolder.getInstance()
                        .downloadFileFromFtpServer("/", "uuids.txt", "C:/Users/Tsinghua/Desktop");
        if (downloadFileFromFtpServer) {
            log.info("下载文件成功");
        } else {
            log.error("下载文件失败");
        }
    }

    @SuppressWarnings("unused")
    private static void upload() {
        boolean uploadSuccess =
                FtpHolder.getInstance()
                        .uploadFileToFtpServer("/test/test", "uuids.txt", "e:/uuids.txt");
        if (uploadSuccess) {
            log.info("上传文件成功");
        } else {
            log.error("上传文件失败");
        }
    }
}
