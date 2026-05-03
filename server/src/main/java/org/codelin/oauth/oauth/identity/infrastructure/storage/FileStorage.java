package org.codelin.oauth.oauth.identity.infrastructure.storage;

import java.io.InputStream;

/**
 * 文件存储接口
 */
public interface FileStorage {

    /**
     * 上传文件
     *
     * @param path        文件路径
     * @param inputStream 文件输入流
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(String path, InputStream inputStream, String contentType);

    /**
     * 下载文件
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    InputStream download(String path);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void delete(String path);

    /**
     * 获取文件访问URL
     *
     * @param path 文件路径
     * @return 访问URL
     */
    String getUrl(String path);

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(String path);
}
