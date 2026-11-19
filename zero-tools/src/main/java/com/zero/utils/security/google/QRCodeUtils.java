package com.zero.utils.security.google;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:20
 * 说明: 二维码工具类
 */
public class QRCodeUtils {
    private static final int                         width  = 300;                                  //二维码宽度(默认)
    private static final int                         height = 300;                                  //二维码高度(默认)
    private static final String                      format = "png";                                //二维码文件格式
    private static final Map<EncodeHintType, Object> hints  = new HashMap<EncodeHintType, Object>();//二维码参数

    static {
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//字符编码
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容错等级 H为最高
        hints.put(EncodeHintType.MARGIN, 2);//边距
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用默认尺寸生成二维码图片。
     * @param content 二维码内容
     * @return
     * 返回值: BufferedImage
     * 返回值说明: 可供后续输出或保存的二维码图像
     */
    public static BufferedImage toBufferedImage(String content) throws WriterException, IOException {
        return toBufferedImage(content, width, height);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用指定宽度和高度生成二维码图片。
     * @param content 二维码内容
     * @param width 图片宽度，单位为像素
     * @param height 图片高度，单位为像素
     * @return
     * 返回值: BufferedImage
     * 返回值说明: 二维码图像
     */
    public static BufferedImage toBufferedImage(String content, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将默认尺寸的二维码输出到给定的流。
     * @param content 二维码内容
     * @param stream 接收二维码图像的输出流
     * @return
     * 返回值: void
     * 返回值说明: 把编码后的二维码图像写入指定流
     */
    public static void writeToStream(String content, OutputStream stream) throws WriterException, IOException {
        writeToStream(content, stream, width, height);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 生成指定尺寸的二维码并输出到给定的流。
     * @param content 二维码内容
     * @param stream 接收二维码图像的输出流
     * @param width 图片宽度，单位为像素
     * @param height 图片高度，单位为像素
     * @return
     * 返回值: void
     * 返回值说明: 把编码后的二维码图像写入指定流
     */
    public static void writeToStream(String content, OutputStream stream, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToStream(bitMatrix, format, stream);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将默认尺寸的二维码图片保存到指定路径。
     * @param content 二维码内容
     * @param path 用于保存二维码图片的文件路径
     * @return
     * 返回值: void
     * 返回值说明: 写入目标文件
     */
    public static void createQRCode(String content, String path) throws WriterException, IOException {
        createQRCode(content, path, width, height);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将指定尺寸的二维码图片保存到目标路径。
     * @param content 二维码内容
     * @param path 用于保存二维码图片的文件路径
     * @param width 图片宽度，单位为像素
     * @param height 图片高度，单位为像素
     * @return
     * 返回值: void
     * 返回值说明: 写入目标文件
     */
    public static void createQRCode(String content, String path, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(bitMatrix, format, new File(path).toPath());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读二维码并输出携带的信息
     * @param inputStream 输入流
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public static String readQrCode(InputStream inputStream) throws IOException {
        //从输入流中获取字符串信息
        BufferedImage image = ImageIO.read(inputStream);
        //将图像转换为二进制位图源
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(bitmap);
        } catch (ReaderException e) {
            e.printStackTrace();
        }
        if (result != null) {
            return result.getText();
        }
        return null;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从 Base64 表示的二维码图片读取其中的文本。
     * @param base64 Base64 编码的二维码图片文本
     * @return
     * 返回值: String
     * 返回值说明: 解码得到的文本
     */
    public static String readQrCode(String base64) throws IOException {
//        byte[] bytes = new BASE64Decoder().decodeBuffer(base64); //将字符串转换为byte数组
    	// 编码
    	byte[] bytes = Base64.getDecoder().decode(base64.getBytes());
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return QRCodeUtils.readQrCode(in);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解码 Base64 图片并交给输入流读取逻辑，可移除 data URL 的 Base64 前缀。
     * @param base64 Base64 编码的二维码图片文本
     * @return
     * 返回值: String
     * 返回值说明: 通过图片读取的二维码文本
     */
    public static String readQrCodeByPicture(String base64) throws IOException {

        if (base64.split("base64,").length > 1) {
            base64 = base64.split("base64,")[1];
        }
//        byte[] bytes = new BASE64Decoder().decodeBuffer(base64); //将字符串转换为byte数组
     // 解码
        byte[] bytes = Base64.getDecoder().decode(base64.getBytes());
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return QRCodeUtils.readQrCode(in);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取文件字节并转换为 Base64 文本。
     * @param path 待编码的本地文件路径
     * @return
     * 返回值: String
     * 返回值说明: 对应文件字节的 Base64 文本
     * @throws Exception 打开或读取文件失败时向上传递
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        byte[] encode = Base64.getEncoder().encode(buffer);
//        return new BASE64Encoder().encode(buffer);
        return new String(encode);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 二维码生成器
     * @param url 链接
     * @return
     * 返回值: String
     * 返回值说明: 二维码生成器
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static String generalQRCode(String url) {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        String binary = null;

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 200, 200, hints);

            // 实现一： 输出图片到指定目录
            // File outputFile = new File("d://1.jpg");
            // MatrixToImageWriter.writeToFile(bitMatrix, "png", outputFile);

            // 实现二：生成二维码图片并将图片转为二进制传递给前台
            // 1、读取文件转换为字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedImage image = toBufferedImage(bitMatrix);
            ////
            ImageIO.write(image, "png", out);
            byte[] bytes = out.toByteArray();

            // 2、将字节数组转为二进制
//            BASE64Encoder encoder = new BASE64Encoder();
//            binary = encoder.encodeBuffer(bytes).trim();
            Encoder encoder = Base64.getEncoder();
            binary = encoder.encodeToString(bytes).trim();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return binary;
    }

    // 其他调用方法
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将二维码矩阵转成可编码输出的图像。
     * @param matrix 待转换的黑白位矩阵
     * @return
     * 返回值: BufferedImage
     * 返回值说明: 由矩阵像素生成的图像
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 用固定样例演示生成二维码图像字符串。
     * @param args 命令行参数，示例不读取
     * @return
     * 返回值: void
     * 返回值说明: 将演示内容或异常输出到控制台
     */
    public static void main(String[] args) {
        try {
//			String encodeBase64File = encodeBase64File("C:\\Users\\Administrator\\Pictures\\核心硬件评测.png");
            String generalQRCode = generalQRCode("www.baidu.com");
            System.out.println(generalQRCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
