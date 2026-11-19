/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zero.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: MultipartFile 鐨勫寘瑁呯被
 */
public class MultipartFileWrapper implements MultipartFile {

    @Setter
    private String name;
    @Setter
    private String originalFilename;
    @Setter
    private String contentType;
    @Setter
    @Getter
    private MultipartFile multipartFile;

    public MultipartFileWrapper(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 提供包装对象生效的 multipart 字段名称。
     * @return
     * 返回值: String
     * 返回值说明: 已设置的覆盖名称，未设置时读取底层文件字段名称
     */
    @Override
    public String getName() {
        return name != null ? name : multipartFile.getName();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 返回用于扩展名解析和存储记录的客户端文件名。
     * @return
     * 返回值: String
     * 返回值说明: 包装对象设置的原始文件名，未覆盖时沿用底层文件
     */
    @Override
    public String getOriginalFilename() {
        return originalFilename != null ? originalFilename : multipartFile.getOriginalFilename();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 确定上传内容当前使用的 MIME 类型。
     * @return
     * 返回值: String
     * 返回值说明: 包装对象覆盖值或底层文件的 MIME 类型，可以为空
     */
    @Override
    @Nullable
    public String getContentType() {
        return contentType != null ? contentType : multipartFile.getContentType();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 由底层 multipart 文件判断是否存在上传内容。
     * @return
     * 返回值: boolean
     * 返回值说明: 底层文件的空内容判断结果
     */
    @Override
    public boolean isEmpty() {
        return multipartFile.isEmpty();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取底层上传文件的内容大小。
     * @return
     * 返回值: long
     * 返回值说明: 上传内容的字节数
     */
    @Override
    public long getSize() {
        return multipartFile.getSize();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 委托底层 multipart 文件读取完整内容。
     * @return
     * 返回值: byte[]
     * 返回值说明: 上传内容的字节数组
     */
    @Override
    public byte[] getBytes() throws IOException {
        return multipartFile.getBytes();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 获取底层 multipart 文件的内容流。
     * @return
     * 返回值: InputStream
     * 返回值说明: 供当前上传步骤读取的输入流
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 把底层 multipart 文件以 Spring 资源形式交给调用方。
     * @return
     * 返回值: Resource
     * 返回值说明: 代表上传文件的 Spring Resource
     */
    @Override
    public Resource getResource() {
        return multipartFile.getResource();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 委托内部 multipart 文件将内容转存到 File。
     * @param dest 转存目标文件
     * @return
     * 返回值: void
     * 返回值说明: 向目标文件转存内容
     */
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        multipartFile.transferTo(dest);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 委托内部 multipart 文件将内容转存到 Path。
     * @param dest 转存目标路径
     * @return
     * 返回值: void
     * 返回值说明: 向目标路径转存内容
     */
    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        multipartFile.transferTo(dest);
    }
}

