package com.zero.system.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.zxing.WriterException;
import com.zero.packageconfig.PackageProperties;
import com.zero.util.EnvironmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zero.common.annotation.DataScope;
import com.zero.common.constant.GoogleAuthConstants;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysGoogle;
import com.zero.system.mapper.SysGoogleMapper;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.service.AdminUserLogoutService;
import com.zero.system.service.ISysGoogleService;
import com.zero.common.utils.google.GoogleAuthenticatorUtils;
import com.zero.common.utils.google.QRCodeUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 用户谷歌动态码绑定服务实现，负责二维码生成、绑定确认、登录动态码校验和解绑；谷歌密钥属于敏感信息，列表返回前必须脱敏
 */
@Service
public class SysGoogleServiceImpl implements ISysGoogleService
{
    @Autowired
    private SysGoogleMapper googleMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RedisCache redisCache;

    /** 解绑提交后撤销目标账号的所有登录会话。 */
    @Autowired
    private AdminUserLogoutService userLogoutService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 查询谷歌动态码绑定列表，受用户和部门数据权限控制，返回前清空明文密钥并补充脱敏展示值
     * @param sysGoogle 查询条件，支持用户信息和绑定信息过滤
     * @return
     * 返回值: List<SysGoogle>
     * 返回值说明: 绑定记录列表，googleAuthKey已清空，googleAuthKeyMask为脱敏值
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysGoogle> selectGoogleList(SysGoogle sysGoogle)
    {
        List<SysGoogle> list = googleMapper.selectGoogleList(sysGoogle);
        for (SysGoogle item : list)
        {
            // 列表只返回绑定状态和脱敏片段，避免谷歌密钥在普通列表接口中泄露。
            item.setBound(StringUtils.isNotEmpty(item.getGoogleAuthKey()));
            item.setGoogleAuthKeyMask(maskGoogleAuthKey(item.getGoogleAuthKey()));
            item.setGoogleAuthKey(null);
        }
        return list;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 根据用户ID查询谷歌动态码绑定记录
     * @param userId 用户ID
     * @return
     * 返回值: SysGoogle
     * 返回值说明: 用户绑定记录；不存在时返回null
     */
    @Override
    public SysGoogle selectGoogleByUserId(Long userId)
    {
        return googleMapper.selectGoogleByUserId(userId);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 查询用户谷歌动态码明文密钥，仅供登录校验等内部安全流程使用
     * @param userId 用户ID
     * @return
     * 返回值: String
     * 返回值说明: 明文谷歌登录密钥；未绑定时抛出业务异常
     */
    @Override
    public String selectGoogleAuthKey(Long userId)
    {
        SysGoogle google = googleMapper.selectGoogleByUserId(userId);
        if (StringUtils.isNull(google) || StringUtils.isEmpty(google.getGoogleAuthKey()))
        {
            throw new ServiceException("用户未绑定谷歌动态码");
        }
        return google.getGoogleAuthKey();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 生成谷歌动态码绑定二维码并写入输出流，临时密钥只写入Redis，用户确认绑定后才落库
     * @param userName 用户名，用于查询用户并生成二维码账号标识
     * @param outputStream 二维码图片输出流
     * @return
     * 返回值: void
     * 返回值说明: 将二维码图片写入输出流，同时缓存15分钟临时绑定密钥
     */
    @Override
    public void writeBindQRCode(String userName, OutputStream outputStream) throws IOException, WriterException
    {
        if (StringUtils.isEmpty(userName))
        {
            throw new ServiceException("用户名不能为空");
        }
        SysUser user = userMapper.selectUserByUserName(userName);
        if (StringUtils.isNull(user))
        {
            throw new ServiceException("用户不存在");
        }
        SysGoogle google = googleMapper.selectGoogleByUserId(user.getUserId());
        if (StringUtils.isNotNull(google) && StringUtils.isNotEmpty(google.getGoogleAuthKey()))
        {
            throw new ServiceException("用户已绑定谷歌动态码");
        }
        String secretKey = GoogleAuthenticatorUtils.createSecretKey();
        //谷歌动态验证码绑定签发方名称
        String issuer = getGoogleIssuerName();
        String qrCodeData = GoogleAuthenticatorUtils.createGoogleAuthQRCodeData(secretKey, userName, issuer);
        // 绑定前密钥只临时放入 Redis，用户输入动态码确认后才落库。
        redisCache.setCacheObject(getBindCacheKey(userName), secretKey, 15, TimeUnit.MINUTES);
        QRCodeUtils.writeToStream(qrCodeData, outputStream);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 14:40
     * 说明: 获取包含运行环境的谷歌绑定签发方名称，冒号和账号由二维码工具统一追加
     * @return
     * 返回值:String
     * 返回值说明：签发方名称，例如Zero管理后台(dev)，不包含末尾冒号和用户账号
     */
    private String getGoogleIssuerName(){
        return "%s(%s)".formatted(
                PackageProperties.GOOGLE_ISSUER_NAME,
                EnvironmentUtil.getEnvEnName()
        );
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 绑定谷歌动态码，校验Redis中的临时密钥和用户输入动态码，通过后新增或更新绑定记录
     * @param userName 用户名
     * @param googleCode 用户输入的6位谷歌动态码
     * @return
     * 返回值: void
     * 返回值说明: 绑定成功后删除临时密钥缓存，失败时抛出业务异常
     */
    @Override
    public void bindGoogle(String userName, String googleCode)
    {
        if (StringUtils.isEmpty(userName))
        {
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isEmpty(googleCode))
        {
            throw new ServiceException("谷歌验证码不能为空");
        }
        SysUser user = userMapper.selectUserByUserName(userName);
        if (StringUtils.isNull(user))
        {
            throw new ServiceException("用户不存在");
        }
        SysGoogle google = googleMapper.selectGoogleByUserId(user.getUserId());
        if (StringUtils.isNotNull(google) && StringUtils.isNotEmpty(google.getGoogleAuthKey()))
        {
            throw new ServiceException("用户已绑定谷歌动态码");
        }
        String secretKey = redisCache.getCacheObject(getBindCacheKey(userName));
        if (StringUtils.isEmpty(secretKey))
        {
            throw new ServiceException("二维码已失效，请重新获取");
        }
        if (!GoogleAuthenticatorUtils.verify(secretKey, googleCode))
        {
            throw new ServiceException("谷歌验证码错误", GoogleAuthConstants.GOOGLE_CODE_ERROR);
        }
        // 兼容历史上已存在空记录的情况：无记录则新增，有记录则补写密钥。
        SysGoogle record = new SysGoogle();
        record.setUserId(user.getUserId());
        record.setGoogleAuthKey(secretKey);
        if (StringUtils.isNull(google))
        {
            record.setCreateBy(userName);
            googleMapper.insertGoogle(record);
        }
        else
        {
            record.setUpdateBy(userName);
            googleMapper.updateGoogleAuthKey(record);
        }
        redisCache.deleteObject(getBindCacheKey(userName));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 校验登录谷歌动态码，未绑定时返回特定业务码供前端打开绑定弹窗
     * @param userId 用户ID
     * @param userName 用户名，用于登录校验上下文
     * @param googleCode 用户输入的6位谷歌动态码
     * @return
     * 返回值: void
     * 返回值说明: 校验通过无返回；未绑定或验证码错误时抛出带业务码的异常
     */
    @Override
    public void validateLoginCode(Long userId, String userName, String googleCode)
    {
        SysGoogle google = googleMapper.selectGoogleByUserId(userId);
        if (StringUtils.isNull(google) || StringUtils.isEmpty(google.getGoogleAuthKey()))
        {
            // 特定业务码用于前端打开首次绑定弹窗，而不是当作普通登录失败处理。
            throw new ServiceException("当前账号未绑定谷歌秘钥,请先进行绑定", GoogleAuthConstants.GOOGLE_BIND_REQUIRED);
        }
        if (StringUtils.isEmpty(googleCode))
        {
            throw new ServiceException("请输入谷歌动态验证码", GoogleAuthConstants.GOOGLE_CODE_NOT_NULL);
        }
        if (!GoogleAuthenticatorUtils.verify(google.getGoogleAuthKey(), googleCode))
        {
            throw new ServiceException("谷歌动态验证码错误", GoogleAuthConstants.GOOGLE_CODE_ERROR);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 清空谷歌绑定密钥，并在解绑提交后强退该账号的所有在线会话。
     * @param userId 用户ID
     * @param updateBy 更新者用户名
     * @return
     * 返回值: int
     * 返回值说明: 受影响行数；用户未绑定时抛出业务异常
     */
    @Override
    @Transactional
    public int unbindGoogle(Long userId, String updateBy)
    {
        SysGoogle google = googleMapper.selectGoogleByUserId(userId);
        if (StringUtils.isNull(google) || StringUtils.isEmpty(google.getGoogleAuthKey()))
        {
            throw new ServiceException("用户未绑定谷歌动态码");
        }
        SysGoogle record = new SysGoogle();
        record.setUserId(userId);
        record.setGoogleAuthKey("");
        record.setUpdateBy(updateBy);
        int rows = googleMapper.updateGoogleAuthKey(record);
        if (rows > 0)
        {
            userLogoutService.afterUsersUnavailable(List.of(userId));
        }
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 生成谷歌动态码绑定临时密钥缓存key
     * @param userName 用户名
     * @return
     * 返回值: String
     * 返回值说明: Redis缓存key，用于保存绑定确认前的临时密钥
     */
    private String getBindCacheKey(String userName)
    {
        return GoogleAuthConstants.GOOGLE_BIND_CACHE_KEY + userName;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 脱敏谷歌登录密钥，只保留前4位和后4位，避免列表接口泄露完整密钥
     * @param googleAuthKey 明文谷歌登录密钥
     * @return
     * 返回值: String
     * 返回值说明: 脱敏后的密钥展示值；空密钥返回空字符串
     */
    private String maskGoogleAuthKey(String googleAuthKey)
    {
        if (StringUtils.isEmpty(googleAuthKey))
        {
            return "";
        }
        if (googleAuthKey.length() <= 8)
        {
            return "****";
        }
        return googleAuthKey.substring(0, 4) + "****" + googleAuthKey.substring(googleAuthKey.length() - 4);
    }
}
