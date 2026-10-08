package com.zero.system.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import com.google.zxing.WriterException;
import com.zero.system.domain.SysGoogle;

/**
 * 用户谷歌动态码绑定 服务层
 */
public interface ISysGoogleService
{
    /**
     * 查询谷歌动态码绑定列表
     *
     * @param sysGoogle 查询条件
     * @return 绑定列表
     */
    public List<SysGoogle> selectGoogleList(SysGoogle sysGoogle);

    /**
     * 根据用户ID查询绑定记录
     *
     * @param userId 用户ID
     * @return 绑定记录
     */
    public SysGoogle selectGoogleByUserId(Long userId);

    /**
     * 查询明文密钥
     *
     * @param userId 用户ID
     * @return 明文密钥
     */
    public String selectGoogleAuthKey(Long userId);

    /**
     * 输出绑定二维码
     *
     * @param userName 用户名
     * @param outputStream 输出流
     */
    public void writeBindQRCode(String userName, OutputStream outputStream) throws IOException, WriterException;

    /**
     * 绑定谷歌动态码
     *
     * @param userName 用户名
     * @param googleCode 动态码
     */
    public void bindGoogle(String userName, String googleCode);

    /**
     * 校验登录动态码
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param googleCode 动态码
     */
    public void validateLoginCode(Long userId, String userName, String googleCode);

    /**
     * 解绑谷歌动态码
     *
     * @param userId 用户ID
     * @param updateBy 更新者
     * @return 结果
     */
    public int unbindGoogle(Long userId, String updateBy);
}
