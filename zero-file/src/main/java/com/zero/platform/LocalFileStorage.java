package com.zero.platform;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.function.Consumer;

import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zero.exception.FileStorageRuntimeException;
import com.zero.vo.FileInfo;
import com.zero.vo.UploadPretreatment;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 本地文件存储。
 */
@Slf4j
@Getter
@Setter
public class LocalFileStorage implements FileStorage {
	
    /* 本地存储路径*/
    private String basePath;
    /* 存储平台 */
    private String platform;
    /* 访问域名 */
    private String domain;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 判断当前系统是否为 Linux。
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    private boolean isLinux() {
    	String os = System.getProperties().getProperty("os.name");
        log.info("osName{}:"+os);
    	if (os != null && os.toLowerCase().startsWith("linux")) {// Linux 系统
    	    return true;
    	}
    	return false;
    }
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 判断当前系统是否为 Windows。
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    private boolean isWindows() {
    	String os = System.getProperties().getProperty("os.name");
        log.info("osName{}:"+os);
    	if (os != null && os.toLowerCase().startsWith("windows")) {// Windows 系统
    	    return true;
    	}
    	return false;
    }
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 通过 本地磁盘 存储原文件及可选的缩略图，并更新文件信息中的存储位置。
     * @param fileInfo 用于回填存储位置的文件信息
     * @param pre 含原文件内容及可选缩略图的预处理结果
     * @return
     * 返回值: boolean
     * 返回值说明: 当前平台的保存结果
     */
    @Override
    public boolean save(FileInfo fileInfo,UploadPretreatment pre) {
    	// 本地磁盘路径
    	String localPath = "";
        String path = fileInfo.getPath();
        boolean linux = this.isLinux();
        log.info("isLinux:{}"+linux);
        if(linux) {
        	if(!basePath.startsWith("/")) { // basePath 不是绝对路径时，按当前运行目录拼接
        		String jarPath = this.getPath();
        		localPath = jarPath + basePath;
        	}
            log.info("当前运行环境为 Linux，localPath:{}", localPath);
        }
        boolean windows = this.isWindows();
        if(windows) {
        	if(!basePath.startsWith("/")) { // basePath 不是绝对路径时，按当前运行目录拼接
        		String jarPath = this.getPath();
        		localPath = jarPath + basePath;
        	}
        	if(localPath.endsWith("/")) {
        		localPath = localPath.substring(0, localPath.length() -1) + File.separator;
        	}
            log.info("当前运行环境为 Windows，localPath:{}", localPath);
        }
        log.info("上传文件磁盘路径：localPath:{}",localPath);
        File newFile = FileUtil.touch(localPath + path,fileInfo.getFilename());
        fileInfo.setBasePath(localPath);
        fileInfo.setUrl(domain + path + fileInfo.getFilename());

        try {
            pre.getFileWrapper().transferTo(newFile);

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                fileInfo.setThUrl(domain + path + fileInfo.getThFilename());
                FileUtil.writeBytes(thumbnailBytes,localPath + path + fileInfo.getThFilename());
            }
            return true;
        } catch (IOException e) {
            FileUtil.del(newFile);
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按历史记录中的存储路径删除 本地磁盘 的对象及关联缩略图。
     * @param fileInfo 携带原文件及缩略图路径信息的文件记录
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件和存在的缩略图均删除成功时返回 true
     */
    @Override
    public boolean delete(FileInfo fileInfo) {
        boolean thumbnailDeleted = fileInfo.getThFilename() == null
                || FileUtil.del(new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getThFilename()));
        boolean fileDeleted = FileUtil.del(new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()));
        return fileDeleted && thumbnailDeleted;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查 本地磁盘 是否仍可找到文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @return
     * 返回值: boolean
     * 返回值说明: 原文件存在时返回 true
     */
    @Override
    public boolean exists(FileInfo fileInfo) {
        return new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()).exists();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 本地磁盘 读取文件记录指向的原文件。
     * @param fileInfo 包含原文件位置的文件信息
     * @param consumer 用于读取输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 在输入流有效期间调用消费回调
     */
    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        try (InputStream in = FileUtil.getInputStream(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename())) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 检查缩略图位置，并从 本地磁盘 读取缩略图。
     * @param fileInfo 包含缩略图位置的文件信息
     * @param consumer 用于读取缩略图输入流的回调
     * @return
     * 返回值: void
     * 返回值说明: 读取后在输入流有效期间调用消费回调
     */
    @Override
    public void downloadTh(FileInfo fileInfo,Consumer<InputStream> consumer) {
        if (StrUtil.isBlank(fileInfo.getThFilename())) {
            throw new FileStorageRuntimeException("文件不存在或无法操作");
        }
        try (InputStream in = FileUtil.getInputStream(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getThFilename())) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取当前运行目录。
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    private String getPath(){  
    	String path = System.getProperty("user.dir");
    	return path + File.separator;
    }  
    
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对比本地存储使用的运行目录、类路径位置和 JVM 工作目录。
     * @param args 命令行参数，示例不读取
     * @return
     * 返回值: void
     * 返回值说明: 向控制台打印各目录的位置
     */
    public static void main(String[] args) throws URISyntaxException, FileNotFoundException, UnsupportedEncodingException {
    	String path = new LocalFileStorage().getPath();
        System.out.println(path);
        String path2 = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(path2);
        System.out.println(System.getProperty("user.dir"));
        String path1 = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        System.out.println(URLDecoder.decode(path1, "utf-8"));
        
	}
    
}

