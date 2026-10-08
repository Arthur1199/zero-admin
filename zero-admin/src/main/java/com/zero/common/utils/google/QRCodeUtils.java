package com.zero.common.utils.google;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: 二维码工具，负责将谷歌动态码绑定内容生成PNG图片并写入输出流
 */
public class QRCodeUtils
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 私有构造方法，避免工具类被实例化
     * @return
     * 返回值: QRCodeUtils
     * 返回值说明: 不允许外部创建实例
     */
    private QRCodeUtils()
    {
    }

    /**
     * 将二维码内容写入图片输出流
     *
     * @param content 二维码内容
     * @param outputStream 图片输出流
     */
    public static void writeToStream(String content, OutputStream outputStream) throws WriterException, IOException
    {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 保留较小白边，登录弹窗中展示更紧凑，同时仍便于扫码识别。
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 280, 280, hints);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ImageIO.write(image, "png", outputStream);
    }
}
