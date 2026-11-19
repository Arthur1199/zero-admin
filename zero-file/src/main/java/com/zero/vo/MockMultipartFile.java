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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 简单的 MultipartFile 实现。
 */
@Getter
public class MockMultipartFile implements MultipartFile {

    /**
     * 文件名。
     */
    private final String name;

    /**
     * 原始文件名。
     */
    private final String originalFilename;

    /**
     * 内容类型。
     */
    @Nullable
    private final String contentType;

    /**
     * 文件内容。
     */
    private final byte[] bytes;


    public MockMultipartFile(String name,InputStream in) {
        this(name,"",null,IoUtil.readBytes(in));
    }

    public MockMultipartFile(String name,@Nullable byte[] bytes) {
        this(name,"",null,bytes);
    }

    public MockMultipartFile(String name,@Nullable String originalFilename,@Nullable String contentType,InputStream in) {
        this(name,originalFilename,contentType,IoUtil.readBytes(in));
    }

    public MockMultipartFile(@Nullable String name,@Nullable String originalFilename,@Nullable String contentType,@Nullable byte[] bytes) {
        this.name = (name != null ? name : "");
        this.originalFilename = (originalFilename != null ? originalFilename : "");
        this.contentType = contentType;
        this.bytes = (bytes != null ? bytes : new byte[0]);
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据内存中的文件字节判断内容是否为空。
     * @return
     * 返回值: boolean
     * 返回值说明: 字节数为 0 时返回 true
     */
    @Override
    public boolean isEmpty() {
        return (this.bytes.length == 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取内存文件内容的实际大小。
     * @return
     * 返回值: long
     * 返回值说明: 文件内容的字节数
     */
    @Override
    public long getSize() {
        return this.bytes.length;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为内存文件内容创建新的读取流。
     * @return
     * 返回值: InputStream
     * 返回值说明: 从内容开头读取的输入流
     */
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 把内存中的 multipart 内容写入目标文件。
     * @param dest 接收字节的目标文件
     * @return
     * 返回值: void
     * 返回值说明: 将文件内容写入指定位置
     */
    @Override
    public void transferTo(File dest) {
        FileUtil.writeBytes(bytes,dest);
    }

}

