package com.zero.vo;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.zero.exception.FileStorageRuntimeException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.function.Consumer;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 文件上传预处理对象。
 */
@Getter
@Setter
@Accessors(chain = true)
public class UploadPretreatment {
    private FileStorageService fileStorageService;
    /**
     * 要上传到的存储平台。
     */
    private String platform;
    /**
     * 要上传的文件包装。
     */
    private MultipartFileWrapper fileWrapper;
    /**
     * 要上传文件的缩略图内容。
     */
    private byte[] thumbnailBytes;
    /**
     * 缩略图后缀，例如 .min.jpg、.png。
     * 只能在缩略图生成前修改后缀中的扩展名部分。
     */
    private String thumbnailSuffix;
    /**
     * 文件所属对象id
     */
    private String objectId;
    /**
     * 文件所属对象类型。
     */
    private String objectType;
    /**
     * 文件存储路径
     */
    private String path = "";

    /**
     * 保存文件名，如果不设置则自动生成
     */
    private String saveFilename;

    /**
     * 缩略图保存文件名，不包含后缀，后缀由 thumbnailSuffix 控制。
     */
    private String saveThFilename;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 记录上传文件关联的业务对象 ID，非空对象转换为字符串。
     * @param objectId 文件关联的业务对象 ID，可为空
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 当前预处理对象，可继续链式设置
     */
    public UploadPretreatment setObjectId(Object objectId) {
        this.objectId = objectId == null ? null : objectId.toString();
        return this;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取文件名。
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getName() {
        return fileWrapper.getName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 更新上传包装对象中的 multipart 字段名。
     * @param name multipart 表单字段名
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 当前预处理对象，可继续链式设置
     */
    public UploadPretreatment setName(String name) {
        fileWrapper.setName(name);
        return this;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取原始文件名。
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getOriginalFilename() {
        return fileWrapper.getOriginalFilename();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 更新上传包装对象保留的客户端原始文件名。
     * @param originalFilename 客户端文件名
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 当前预处理对象，可继续链式设置
     */
    public UploadPretreatment setOriginalFilename(String originalFilename) {
        fileWrapper.setOriginalFilename(originalFilename);
        return this;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 对当前上传文件的图像执行指定处理，并用生成内容替换上传体。
     * @param consumer 用于设置缩放、水印等操作的回调
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 可继续配置的当前上传预处理对象
     */
    public UploadPretreatment image(Consumer<Thumbnails.Builder<? extends InputStream>> consumer) {
        try (InputStream in = fileWrapper.getInputStream()) {
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(in);
            consumer.accept(builder);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.toOutputStream(out);
            MultipartFile mf = fileWrapper.getMultipartFile();
            fileWrapper.setMultipartFile(new MockMultipartFile(mf.getName(),mf.getOriginalFilename(),null,out.toByteArray()));
            return this;
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件处理失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将当前图片缩放到给定尺寸范围。
     * @param width 最大宽度，单位像素
     * @param height 最大高度，单位像素
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 可继续配置的当前上传预处理对象
     */
    public UploadPretreatment image(int width,int height) {
        return image(th -> th.size(width,height));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用默认 200 × 200 像素范围缩放当前图片。
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 可继续配置的当前上传预处理对象
     */
    public UploadPretreatment image() {
        return image(th -> th.size(200,200));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 清除已生成的缩略图数据，避免上传时写入该缩略图。
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 可继续配置的当前上传预处理对象
     */
    public UploadPretreatment clearThumbnail() {
        thumbnailBytes = null;
        return this;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 已有缩略图时以该图为输入，否则从当前上传图片开始生成。
     * @param consumer 用于设置缩略图处理方式的回调
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有最新缩略图内容的预处理对象
     */
    public UploadPretreatment thumbnail(Consumer<Thumbnails.Builder<? extends InputStream>> consumer) {
        try {
            return thumbnail(consumer,thumbnailBytes != null ? new ByteArrayInputStream(thumbnailBytes) : fileWrapper.getInputStream());
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件处理失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用调用方提供的文件生成缩略图，以配置后缀决定默认输出格式。
     * @param consumer 缩略图处理回调
     * @param file 用于生成缩略图的图片文件
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有新缩略图内容的预处理对象
     */
    public UploadPretreatment thumbnail(Consumer<Thumbnails.Builder<? extends InputStream>> consumer,MultipartFile file) {
        try {
            return thumbnail(consumer,file.getInputStream());
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件处理失败", e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取传入的图片，默认按缩略图后缀选择输出格式，结束后关闭输入流。
     * @param consumer 缩略图处理回调
     * @param in 由当前方法接管关闭的图片输入流
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有新缩略图内容的预处理对象
     */
    public UploadPretreatment thumbnail(Consumer<Thumbnails.Builder<? extends InputStream>> consumer,InputStream in) {
        try {
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(in);
            builder.outputFormat(FileNameUtil.extName(thumbnailSuffix));
            consumer.accept(builder);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.toOutputStream(out);
            thumbnailBytes = out.toByteArray();
            return this;
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件处理失败", e);
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 把缩略图缩放到给定尺寸范围，保持缩略图后缀配置。
     * @param width 最大宽度，单位像素
     * @param height 最大高度，单位像素
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有缩放后缩略图内容的预处理对象
     */
    public UploadPretreatment thumbnail(int width,int height) {
        return thumbnail(th -> th.size(width,height));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成默认 200 × 200 像素范围内的缩略图。
     * @return
     * 返回值: UploadPretreatment
     * 返回值说明: 持有缩略图内容的预处理对象
     */
    public UploadPretreatment thumbnail() {
        return thumbnail(200,200);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将当前上传内容和预处理结果交给存储服务。
     * @return
     * 返回值: FileInfo
     * 返回值说明: 有成功结果时为文件信息，存储或记录返回失败时为 null，异常向调用方传递
     */
    public FileInfo upload() {
        return fileStorageService.upload(this);
    }
}

