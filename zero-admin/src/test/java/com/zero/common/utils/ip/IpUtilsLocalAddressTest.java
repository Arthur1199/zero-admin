package com.zero.common.utils.ip;

import java.net.InetAddress;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 使用固定地址验证节点IP选择，不依赖测试机器网卡或外部网络
 */
class IpUtilsLocalAddressTest
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 即使其它地址排在前面，也应优先选择站点本地IPv4
     * @return
     * 返回值: void
     * 返回值说明: 内网地址优先且无内网时保留其它有效IPv4
     */
    @Test
    void privateAddressTakesPriority() throws Exception
    {
        var external = InetAddress.getByAddress(new byte[] {(byte) 203, 0, 113, 1});
        var internal = InetAddress.getByAddress(new byte[] {10, 20, 30, 40});
        assertThat(IpUtils.selectAddress(List.of(external, internal))).isEqualTo("10.20.30.40");
        assertThat(IpUtils.selectAddress(List.of(external))).isEqualTo("203.0.113.1");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 无可用IPv4时安全兜底，不把回环、未指定或链路本地地址当作发送节点候选
     * @return
     * 返回值: void
     * 返回值说明: 无候选时返回回环，重复读取缓存得到一致值
     */
    @Test
    void invalidCandidatesFallBackSafely() throws Exception
    {
        var addresses = List.of(InetAddress.getByAddress(new byte[] {127, 0, 0, 1}),
                InetAddress.getByAddress(new byte[] {0, 0, 0, 0}),
                InetAddress.getByAddress(new byte[] {(byte) 169, (byte) 254, 1, 1}),
                InetAddress.getByAddress(new byte[16]));
        assertThat(IpUtils.selectAddress(addresses)).isEqualTo(InetAddress.getLoopbackAddress().getHostAddress());
        assertThat(IpUtils.selectAddress(null)).isEqualTo(InetAddress.getLoopbackAddress().getHostAddress());
        assertThat(IpUtils.getLocalIpAddress()).isNotBlank().isEqualTo(IpUtils.getLocalIpAddress());
    }
}
