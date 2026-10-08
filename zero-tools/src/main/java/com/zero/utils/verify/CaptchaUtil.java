package com.zero.utils.verify;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:45
 * 说明: 历史验证码对象兼容入口，验证码生成和图片绘制统一委托给VerifyCodeUtils
 */
public class CaptchaUtil {

    /**
     * 默认验证码图片宽度
     */
    private static final int DEFAULT_WIDTH = 130;

    /**
     * 默认验证码图片高度
     */
    private static final int DEFAULT_HEIGHT = 47;

    /**
     * 默认验证码长度
     */
    private static final int DEFAULT_CODE_SIZE = 4;

    /**
     * 验证码图片
     */
    private BufferedImage image;

    /**
     * 验证码文本
     */
    private String code;

    /**
     * session中保存验证码的历史字段名
     */
    public final String SESSION_CODE_NAME = "code";

    private CaptchaUtil() {
        init();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:45
     * 说明: 创建验证码实例，保留旧代码中的静态入口命名
     * @return
     * 返回值: CaptchaUtil
     * 返回值说明: 包含验证码文本和验证码图片的实例
     */
    public static CaptchaUtil Instance() {
        return new CaptchaUtil();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:45
     * 说明: 获取验证码图片
     * @return
     * 返回值: BufferedImage
     * 返回值说明: 当前实例生成的验证码图片
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:45
     * 说明: 获取验证码文本
     * @return
     * 返回值: String
     * 返回值说明: 当前实例生成的验证码文本
     */
    public String getCode() {
        return this.code;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:45
     * 说明: 初始化验证码文本和图片，图片生成失败时抛出运行时异常，避免调用方拿到空图片
     * @return
     * 返回值: void
     * 返回值说明: 写入当前实例的code和image字段
     */
    private void init() {
        this.code = VerifyCodeUtils.generateVerifyCode(DEFAULT_CODE_SIZE);
        try {
            this.image = VerifyCodeUtils.outputImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, this.code);
        } catch (IOException e) {
            throw new IllegalStateException("generate captcha image failed", e);
        }
    }
}
