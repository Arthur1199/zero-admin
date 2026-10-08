package com.zero.utils.file.disk;
import java.io.File;

/**
 * 作者: Jack
 * 时间: 2025年3月5日18:35:09
 * 说明: 磁盘空间检测工具列
 *
 */
public class DiskSpaceSummaryUtil {
	
	/**
	 * 作者:Jack
	 * 日期:2025年3月5日18:36:52
	 * @return
	 * 返回值:DiskSpaceSummary
	 * 返回值说明:检测磁盘
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
     * 格式化字节大小为合适的单位（KB、MB、GB）
     * @param size 字节大小
     * @return 格式化后的字符串
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