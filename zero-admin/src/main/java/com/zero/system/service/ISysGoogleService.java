package com.zero.system.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import com.google.zxing.WriterException;
import com.zero.system.domain.SysGoogle;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 用户谷歌动态码绑定 服务层
 */
public interface ISysGoogleService
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询谷歌动态码绑定列表
     * @param sysGoogle 查询条件
     * @return
     * 返回值: List<SysGoogle>
     * 返回值说明: 绑定列表
     */
    public List<SysGoogle> selectGoogleList(SysGoogle sysGoogle);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 根据用户ID查询绑定记录
     * @param userId 用户ID
     * @return
     * 返回值: SysGoogle
     * 返回值说明: 绑定记录
     */
    public SysGoogle selectGoogleByUserId(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 查询明文密钥
     * @param userId 用户ID
     * @return
     * 返回值: String
     * 返回值说明: 明文密钥
     */
    public String selectGoogleAuthKey(Long userId);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为指定账号生成动态码绑定二维码并写入响应流。
     * @param userName 待绑定的账号
     * @param outputStream 接收二维码的流
     * @return
     * 返回值: void
     * 返回值说明: 输出绑定二维码图片
     */
    public void writeBindQRCode(String userName, OutputStream outputStream) throws IOException, WriterException;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 使用当前临时绑定信息核验动态码，并保存正式绑定。
     * @param userName 待绑定的账号
     * @param googleCode 从身份验证器获取的当前动态码
     * @return
     * 返回值: void
     * 返回值说明: 验证通过后建立账号与动态码密钥的绑定
     */
    public void bindGoogle(String userName, String googleCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 核验登录用户提交的动态码是否满足登录规则。
     * @param userId 登录用户的主键
     * @param userName 登录账号
     * @param googleCode 用户提交的动态码
     * @return
     * 返回值: void
     * 返回值说明: 校验失败时中止本次登录
     */
    public void validateLoginCode(Long userId, String userName, String googleCode);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解绑谷歌动态码
     * @param userId 用户ID
     * @param updateBy 更新者
     * @return
     * 返回值: int
     * 返回值说明: 解除绑定操作影响的记录数
     */
    public int unbindGoogle(Long userId, String updateBy);
}
