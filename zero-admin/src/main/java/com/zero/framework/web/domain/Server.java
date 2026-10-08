package com.zero.framework.web.domain;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import com.zero.common.utils.Arith;
import com.zero.common.utils.ip.IpUtils;
import com.zero.framework.web.domain.server.Cpu;
import com.zero.framework.web.domain.server.Jvm;
import com.zero.framework.web.domain.server.Mem;
import com.zero.framework.web.domain.server.Sys;
import com.zero.framework.web.domain.server.SysFile;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 服务器相关信息
 */
public class Server
{
    /**
     * OSHIWAITSECOND字段
     */
    private static final int OSHI_WAIT_SECOND = 1000;

    /**
     * CPU相关信息
     */
    private Cpu cpu = new Cpu();

    /**
     * 內存相关信息
     */
    private Mem mem = new Mem();

    /**
     * JVM相关信息
     */
    private Jvm jvm = new Jvm();

    /**
     * 服务器相关信息
     */
    private Sys sys = new Sys();

    /**
     * 磁盘相关信息
     */
    private List<SysFile> sysFiles = new LinkedList<SysFile>();
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Cpu
     * @return
     * 返回值: Cpu
     * 返回值说明：获取Cpu
     */

    public Cpu getCpu()
    {
        return cpu;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Cpu
     * @param cpu cpu参数
     * @return
     * 返回值: void
     * 返回值说明：设置Cpu
     */

    public void setCpu(Cpu cpu)
    {
        this.cpu = cpu;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Mem
     * @return
     * 返回值: Mem
     * 返回值说明：获取Mem
     */

    public Mem getMem()
    {
        return mem;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Mem
     * @param mem mem参数
     * @return
     * 返回值: void
     * 返回值说明：设置Mem
     */

    public void setMem(Mem mem)
    {
        this.mem = mem;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取Jvm
     * @return
     * 返回值: Jvm
     * 返回值说明：获取Jvm
     */

    public Jvm getJvm()
    {
        return jvm;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Jvm
     * @param jvm jvm参数
     * @return
     * 返回值: void
     * 返回值说明：设置Jvm
     */

    public void setJvm(Jvm jvm)
    {
        this.jvm = jvm;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取系统
     * @return
     * 返回值: Sys
     * 返回值说明：获取系统
     */

    public Sys getSys()
    {
        return sys;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置系统
     * @param sys 系统参数
     * @return
     * 返回值: void
     * 返回值说明：设置系统
     */

    public void setSys(Sys sys)
    {
        this.sys = sys;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取系统Files
     * @return
     * 返回值: List<SysFile>
     * 返回值说明：获取系统Files
     */

    public List<SysFile> getSysFiles()
    {
        return sysFiles;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置系统Files
     * @param sysFiles 系统Files参数
     * @return
     * 返回值: void
     * 返回值说明：设置系统Files
     */

    public void setSysFiles(List<SysFile> sysFiles)
    {
        this.sysFiles = sysFiles;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 复制To
     * @return
     * 返回值: void
     * 返回值说明：复制To
     */

    public void copyTo() throws Exception
    {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        setCpuInfo(hal.getProcessor());

        setMemInfo(hal.getMemory());

        setSysInfo();

        setJvmInfo();

        setSysFiles(si.getOperatingSystem());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置CPU信息
     * @param processor processor参数
     * @return
     * 返回值: void
     * 返回值说明：设置CPU信息
     */
    private void setCpuInfo(CentralProcessor processor)
    {
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setTotal(totalCpu);
        cpu.setSys(cSys);
        cpu.setUsed(user);
        cpu.setWait(iowait);
        cpu.setFree(idle);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置内存信息
     * @param memory memory参数
     * @return
     * 返回值: void
     * 返回值说明：设置内存信息
     */
    private void setMemInfo(GlobalMemory memory)
    {
        mem.setTotal(memory.getTotal());
        mem.setUsed(memory.getTotal() - memory.getAvailable());
        mem.setFree(memory.getAvailable());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置服务器信息
     * @return
     * 返回值: void
     * 返回值说明：设置服务器信息
     */
    private void setSysInfo()
    {
        Properties props = System.getProperties();
        sys.setComputerName(IpUtils.getHostName());
        sys.setComputerIp(IpUtils.getHostIp());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置Java虚拟机
     * @return
     * 返回值: void
     * 返回值说明：设置Java虚拟机
     */
    private void setJvmInfo() throws UnknownHostException
    {
        Properties props = System.getProperties();
        jvm.setTotal(Runtime.getRuntime().totalMemory());
        jvm.setMax(Runtime.getRuntime().maxMemory());
        jvm.setFree(Runtime.getRuntime().freeMemory());
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置磁盘信息
     * @param os os参数
     * @return
     * 返回值: void
     * 返回值说明：设置磁盘信息
     */
    private void setSysFiles(OperatingSystem os)
    {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray)
        {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFile sysFile = new SysFile();
            sysFile.setDirName(fs.getMount());
            sysFile.setSysTypeName(fs.getType());
            sysFile.setTypeName(fs.getName());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            sysFile.setUsage(Arith.mul(Arith.div(used, total, 4), 100));
            sysFiles.add(sysFile);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 字节转换
     * @param size 字节大小
     * @return
     * 返回值: 转换后值
     * 返回值说明：字节转换
     */
    public String convertFileSize(long size)
    {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb)
        {
            return String.format("%.1f GB", (float) size / gb);
        }
        else if (size >= mb)
        {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }
        else if (size >= kb)
        {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }
        else
        {
            return String.format("%d B", size);
        }
    }
}
