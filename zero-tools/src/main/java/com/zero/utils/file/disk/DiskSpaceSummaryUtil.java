package com.zero.utils.file.disk;
import java.io.File;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 扫描系统可见的磁盘根目录并汇总空间，用于监控可用容量。
 */
public class DiskSpaceSummaryUtil {
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 合计各磁盘根目录的容量和可用空间，剩余总空间小于等于 5 GiB 时标记告警。
	 * @return
	 * 返回值: DiskSpaceSummary
	 * 返回值说明: 格式化后的磁盘空间汇总，系统无可用磁盘根目录时返回 null
	 */
	public static DiskSpaceSummary detectDisk() {
		DiskSpaceSummary summary = new DiskSpaceSummary();
		  // 初始化统计变量
        long totalCapacity = 0;  // 所有磁盘的总容量
        long totalUsableSpace = 0;  // 所有磁盘的可用空间
        long totalUsedSpace = 0;  // 所有磁盘的已用空间

        // 获取系统所有磁盘根目录
        File[] disks = File.listRoots();
        if (disks == null || disks.length == 0) {
            System.out.println("未检测到任何磁盘分区。");
            return null;
        }
//        System.out.println("检测到的磁盘分区及空间信息：");
        for (File disk : disks) {
            // 获取当前磁盘的空间信息
            long diskTotalCapacity = disk.getTotalSpace();
            long diskUsableSpace = disk.getUsableSpace();
            long diskUsedSpace = diskTotalCapacity - diskUsableSpace;
            // 累加到总统计中
            totalCapacity += diskTotalCapacity;
            totalUsableSpace += diskUsableSpace;
            totalUsedSpace += diskUsedSpace;
//            // 打印当前磁盘信息
//            System.out.println("磁盘路径：" + disk.getAbsolutePath());
//            System.out.println("总容量：" + formatSize(diskTotalCapacity));
//            System.out.println("已用空间：" + formatSize(diskUsedSpace));
//            System.out.println("可用空间：" + formatSize(diskUsableSpace));
//            System.out.println("------------------------------------------------");
        }
        // 打印汇总信息
//        System.out.println("所有磁盘的汇总信息：");
//        System.out.println("总容量：" + formatSize(totalCapacity));
//        System.out.println("已用空间：" + formatSize(totalUsedSpace));
//        System.out.println("可用空间：" + formatSize(totalUsableSpace));
        String totalCapacityStr = formatSize(totalCapacity);
        String totalUsedSpaceStr = formatSize(totalUsedSpace);
        String totalUsableSpaceStr = formatSize(totalUsableSpace);
        summary.setTotalCapacity(totalCapacityStr);
        summary.setTotalUsableSpace(totalUsableSpaceStr);
        summary.setTotalUsedSpace(totalUsedSpaceStr);
        double  sizeInGB = totalUsableSpace / (1024.0 * 1024 * 1024);
//        System.out.println(sizeInGB);
        if(sizeInGB <= 5) { //小5G开始报警
        	summary.setWarning(true);
        }
        return summary;
	}
	
	
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 扫描系统可见磁盘并打印容量、占用及可用空间。
     * @param args 命令行参数，示例不读取
     * @return
     * 返回值: void
     * 返回值说明: 向控制台展示汇总结果
     */
    public static void main(String[] args) {
        // 初始化统计变量
        long totalCapacity = 0;  // 所有磁盘的总容量
        long totalUsableSpace = 0;  // 所有磁盘的可用空间
        long totalUsedSpace = 0;  // 所有磁盘的已用空间

        // 获取系统所有磁盘根目录
        File[] disks = File.listRoots();
        if (disks == null || disks.length == 0) {
            System.out.println("未检测到任何磁盘分区。");
            return;
        }

        System.out.println("检测到的磁盘分区及空间信息：");
        for (File disk : disks) {
            // 获取当前磁盘的空间信息
            long diskTotalCapacity = disk.getTotalSpace();
            long diskUsableSpace = disk.getUsableSpace();
            long diskUsedSpace = diskTotalCapacity - diskUsableSpace;

            // 累加到总统计中
            totalCapacity += diskTotalCapacity;
            totalUsableSpace += diskUsableSpace;
            totalUsedSpace += diskUsedSpace;

            // 打印当前磁盘信息
            System.out.println("磁盘路径：" + disk.getAbsolutePath());
            System.out.println("总容量：" + formatSize(diskTotalCapacity));
            System.out.println("已用空间：" + formatSize(diskUsedSpace));
            System.out.println("可用空间：" + formatSize(diskUsableSpace));
            System.out.println("------------------------------------------------");
        }

        // 打印汇总信息
        System.out.println("所有磁盘的汇总信息：");
        System.out.println("总容量：" + formatSize(totalCapacity));
        System.out.println("已用空间：" + formatSize(totalUsedSpace));
        System.out.println("可用空间：" + formatSize(totalUsableSpace));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 格式化字节大小为合适的单位（KB、MB、GB）
     * @param size 字节大小
     * @return
     * 返回值: String
     * 返回值说明: 格式化后的字符串
     */
    private static String formatSize(long size) {
        if (size < 1024) {
            return size + " 字节";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}