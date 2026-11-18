package com.zero.system.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import javax.imageio.ImageIO;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.redis.RedisCache;
import com.zero.packageconfig.PackageProperties;
import com.zero.system.mapper.SysGoogleMapper;
import com.zero.system.mapper.SysUserMapper;
import com.zero.system.service.impl.SysGoogleServiceImpl;
import com.zero.util.EnvironmentUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/18
 * 说明: 谷歌绑定二维码标签回归，生成并解码真实PNG，不连接数据库、Redis或手机，不输出临时密钥
 */
class GoogleBindingLabelTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18
     * 说明: 各环境二维码只在签发方与账号之间保留一个冒号，issuer参数不包含账号分隔符
     * @return
     * 返回值: void
     * 返回值说明: PNG解码标签为Zero管理后台(环境):admin，防止重复冒号及空账号识别
     */
    @Test
    void decodedQrCodeHasSingleSeparatorAndCorrectIssuer() throws Exception {
        try (var environment = mockStatic(EnvironmentUtil.class)) {
            for (String name : new String[] { "dev", "test", "prod", "unknown" }) {
                environment.when(EnvironmentUtil::getEnvEnName).thenReturn(name);
                URI uri = createAndDecode("admin");
                String issuer = "%s(%s)".formatted(PackageProperties.GOOGLE_ISSUER_NAME, name);
                assertThat(uri.getScheme()).isEqualTo("otpauth");
                assertThat(uri.getHost()).isEqualTo("totp");
                assertThat(uri.getPath()).isEqualTo("/" + issuer + ":admin");
                String issuerParameter = Arrays.stream(uri.getRawQuery().split("&"))
                        .filter(value -> value.startsWith("issuer=")).findFirst().orElseThrow();
                assertThat(URLDecoder.decode(issuerParameter.substring("issuer=".length()), StandardCharsets.UTF_8))
                        .isEqualTo(issuer).doesNotContain(":");
            }
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18
     * 说明: 中文及加号等账号字符经过URI编码和二维码解码后保持原文
     * @return
     * 返回值: void
     * 返回值说明: 只改变展示标签，不截断或替换登录账号
     */
    @Test
    void accountTextSurvivesEncoding() throws Exception {
        try (var environment = mockStatic(EnvironmentUtil.class)) {
            environment.when(EnvironmentUtil::getEnvEnName).thenReturn("dev");
            String account = "运营+admin@example.com";
            assertThat(createAndDecode(account).getPath())
                    .isEqualTo("/" + PackageProperties.GOOGLE_ISSUER_NAME + "(dev):" + account);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18
     * 说明: 使用真实绑定二维码生成链路，所有持久化访问替换为隔离替身
     * @param account 测试登录账号，不使用真实账号或真实绑定密钥
     * @return
     * 返回值: URI
     * 返回值说明: 从内存PNG解码出的otpauth地址，不写文件、不记录二维码或密钥
     */
    private URI createAndDecode(String account) throws Exception {
        var users = mock(SysUserMapper.class);
        var google = mock(SysGoogleMapper.class);
        var cache = mock(RedisCache.class);
        var service = new SysGoogleServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", users);
        ReflectionTestUtils.setField(service, "googleMapper", google);
        ReflectionTestUtils.setField(service, "redisCache", cache);
        var user = new SysUser();
        user.setUserId(7L);
        user.setUserName(account);
        when(users.selectUserByUserName(account)).thenReturn(user);
        try (var output = new ByteArrayOutputStream()) {
            service.writeBindQRCode(account, output);
            var image = ImageIO.read(new ByteArrayInputStream(output.toByteArray()));
            var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            // 内存生成的规则二维码按纯图解码，避免把定位算法的不稳定误判为标签内容错误。
            return URI.create(new MultiFormatReader().decode(bitmap, Map.of(DecodeHintType.PURE_BARCODE, true)).getText());
        }
    }
}

