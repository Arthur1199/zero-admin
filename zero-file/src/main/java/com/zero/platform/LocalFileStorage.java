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
 * 本地文件存储。
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
     * 判断当前系统是否为 Linux。
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
     * 判断当前系统是否为 Windows。
     */
    private boolean isWindows() {
    	String os = System.getProperties().getProperty("os.name");
        log.info("osName{}:"+os);
    	if (os != null && os.toLowerCase().startsWith("windows")) {// Windows 系统
    	    return true;
    	}
    	return false;
    }
    
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

    @Override
    public boolean delete(FileInfo fileInfo) {
        if (fileInfo.getThFilename() != null) {   // 删除缩略图
            FileUtil.del(new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getThFilename()));
        }
        return FileUtil.del(new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()));
    }


    @Override
    public boolean exists(FileInfo fileInfo) {
        return new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()).exists();
    }

    @Override
    public void download(FileInfo fileInfo,Consumer<InputStream> consumer) {
        try (InputStream in = FileUtil.getInputStream(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename())) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件存储操作失败", e);
        }
    }

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
     * 获取当前运行目录。
     */
    private String getPath(){  
    	String path = System.getProperty("user.dir");
    	return path + File.separator;
    }  
    
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

