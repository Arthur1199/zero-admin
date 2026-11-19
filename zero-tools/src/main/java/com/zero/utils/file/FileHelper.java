package com.zero.utils.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.FilenameUtils;

import com.zero.utils.common.StringHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:18
 * 说明: 文件处理工具类，提供路径解析、文件读写、目录拷贝、删除、重命名和CRC校验能力
 */
public class FileHelper {

	
	/**
	 * CRC读取缓冲区大小，固定为1KB。
	 */
	private final static int KB_1 = 1024;

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 计算文件的CRC32校验值
	 * @param file 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
	public static String getFileCRCCode(File file) throws Exception {
		FileInputStream is = new FileInputStream(file);
		CRC32 crc32 = new CRC32();
		CheckedInputStream cis = new CheckedInputStream(is, crc32);
		byte[] buffer = null;
		buffer = new byte[KB_1];
		while (cis.read(buffer) != -1) {
		}
		is.close();
		buffer = null;
		return Long.toHexString(crc32.getValue());
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 计算字符串的CRC32校验值
	 * @param string 写入的数据内容
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
	public static String getStringCRCCode(String string) throws Exception {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
		CRC32 crc32 = new CRC32();
		CheckedInputStream checkedinputstream = new CheckedInputStream(inputStream, crc32);
		byte[] buffer = null;
		buffer = new byte[KB_1];
		while (checkedinputstream.read(buffer) != -1) {
		}
		inputStream.close();
		buffer = null;
		return Long.toHexString(crc32.getValue());
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拼接基础路径和文件名
	 * @param basePath 文件对象或文件路径
	 * @param fullFilenameToAdd 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String concat(String basePath, String fullFilenameToAdd) {
		return FilenameUtils.concat(basePath, fullFilenameToAdd);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 获取不带扩展名的文件名
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String getBaseName(String filename) {
		return FilenameUtils.getBaseName(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 获取带扩展名的文件名
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String getFileName(String filename) {
		return FilenameUtils.getName(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 获取文件所在目录路径，包含结尾分隔符
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String getFullPath(String filename) {
		return FilenameUtils.getFullPath(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 获取文件所在目录路径，不包含结尾分隔符
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String getFullPathNoEndSeparator(String filename) {
		return FilenameUtils.getFullPathNoEndSeparator(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 判断文件扩展名是否匹配
	 * @param filename 文件对象或文件路径
	 * @param extension 方法参数
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean isExtension(String filename, String extension) {
		return FilenameUtils.isExtension(filename, extension);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 判断文件扩展名是否匹配
	 * @param filename 文件对象或文件路径
	 * @param extensions 方法参数
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean isExtension(String filename, String[] extensions) {
		return FilenameUtils.isExtension(filename, extensions);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 规范化文件路径
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String normalize(String filename) {
		return FilenameUtils.normalize(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 规范化文件路径并移除结尾分隔符
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String normalizeNoEndSeparator(String filename) {
		return FilenameUtils.normalizeNoEndSeparator(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将路径分隔符转为Unix格式
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String separatorsToUnix(String path) {
		return FilenameUtils.separatorsToUnix(path);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将路径分隔符转为Windows格式
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String separatorsToWindows(String path) {
		return FilenameUtils.separatorsToWindows(path);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将路径分隔符转为当前系统格式
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String separatorsToSystem(String path) {
		return FilenameUtils.separatorsToSystem(path);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 提取文件扩展名
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String getExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 移除文件扩展名
	 * @param filename 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String removeExtension(String filename) {
		return FilenameUtils.removeExtension(filename);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 清空目录内容，保留目录本身
	 * @param directory 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean cleanDirectory(File directory) {
		try {
			org.apache.commons.io.FileUtils.cleanDirectory(directory);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("清除目录出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝目录内容到目标目录
	 * @param srcDir 目录对象或目录路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyDirectory(File srcDir, File destDir) {
		return copyDirectory(srcDir, destDir, true);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝目录内容到目标目录
	 * @param srcDir 目录对象或目录路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyDirectory(String srcDir, String destDir) {
		return copyDirectory(new File(srcDir), new File(destDir));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝目录内容到目标目录
	 * @param srcDir 目录对象或目录路径
	 * @param destDir 目录对象或目录路径
	 * @param preserveFileDate 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyDirectory(File srcDir, File destDir, boolean preserveFileDate) {
		try {
			org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir, preserveFileDate);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("复制目录出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将源目录拷贝到目标目录内部
	 * @param srcDir 目录对象或目录路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyDirectoryToDirectory(File srcDir, File destDir) {
		try {
			org.apache.commons.io.FileUtils.copyDirectoryToDirectory(srcDir, destDir);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("复制目录出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将源目录拷贝到目标目录内部
	 * @param srcDir 目录对象或目录路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyDirectoryToDirectory(String srcDir, String destDir) {
		return copyDirectoryToDirectory(new File(srcDir), new File(destDir));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝文件到目标文件
	 * @param srcFile 文件对象或文件路径
	 * @param destFile 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyFile(File srcFile, File destFile) {
		return copyFile(srcFile, destFile, true);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝文件到目标文件
	 * @param srcFile 文件对象或文件路径
	 * @param destFile 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyFile(String srcFile, String destFile) {
		return copyFile(new File(srcFile), new File(destFile));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝文件到目标文件
	 * @param srcFile 文件对象或文件路径
	 * @param destFile 文件对象或文件路径
	 * @param preserveFileDate 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyFile(File srcFile, File destFile, boolean preserveFileDate) {
		try {
			org.apache.commons.io.FileUtils.copyFile(srcFile, destFile, preserveFileDate);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("复制文件出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝文件到目标目录
	 * @param srcFile 文件对象或文件路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyFileToDirectory(File srcFile, File destDir) {
		try {
			org.apache.commons.io.FileUtils.copyFileToDirectory(srcFile, destDir);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("复制文件出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 拷贝文件到目标目录
	 * @param srcFile 文件对象或文件路径
	 * @param destDir 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean copyFileToDirectory(String srcFile, String destDir) {
		return copyFileToDirectory(new File(srcFile), new File(destDir));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 删除目录及其全部内容
	 * @param directory 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean deleteDirectory(String directory) {
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(new File(directory));
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("删除目录出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 删除指定文件
	 * @param file 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean deleteFile(String file) {
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File(file));
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("删除文件出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 递归创建目录
	 * @param directory 目录对象或目录路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean createDirectory(String directory) {
		try {
			org.apache.commons.io.FileUtils.forceMkdir(new File(directory));
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("创建目录出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将文件读取为字节数组
	 * @param file 文件对象或文件路径
	 * @return
	 * 返回值: byte[]
	 * 返回值说明: 文件字节数组，读取失败时返回null
	 */
	public static byte[] readFileToByteArray(String file) {
		try {
			byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(file));
			return bytes;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("读取文件出错");
		}
		return null;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将文件读取为字符串
	 * @param file 文件对象或文件路径
	 * @param encoding 文件编码，空值时使用GBK
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String readFileToString(String file, String encoding) {
		try {
			if (StringHelper.isEmpty(encoding)) {
				encoding = "GBK";
			}
			String content = org.apache.commons.io.FileUtils.readFileToString(new File(file), encoding);
			return content;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("读取文件出错");
		}
		return "";
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将文件读取为字符串
	 * @param file 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 */
	public static String readFileToString(String file) {
		return readFileToString(file, "GBK");
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按 GBK 编码逐行读取本地文本文件。
	 * @param file 要读取的本地文件路径
	 * @return
	 * 返回值: List
	 * 返回值说明: 文件的文本行列表，读取失败时返回 null
	 */
	@SuppressWarnings("rawtypes")
	public static List readLines(String file) {
		return readLines(file, "GBK");
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 按指定字符集逐行读取本地文本文件。
	 * @param file 要读取的本地文件路径
	 * @param encoding 文件的字符集名称，空值时使用 GBK
	 * @return
	 * 返回值: List
	 * 返回值说明: 文件的文本行列表，读取失败时返回 null
	 */
	@SuppressWarnings("rawtypes")
	public static List readLines(String file, String encoding) {

		try {
			if (StringHelper.isEmpty(encoding)) {
				encoding = "GBK";
			}
			List lineList = org.apache.commons.io.FileUtils.readLines(new File(file), encoding);
			return lineList;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("读取文件出错");
		}
		return null;

	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 递归计算目录大小
	 * @param directory 目录对象或目录路径
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long sizeOfDirectory(String directory) {
		return org.apache.commons.io.FileUtils.sizeOfDirectory(new File(directory));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将数据写入文件，文件不存在时自动创建
	 * @param file 文件对象或文件路径
	 * @param data 写入的数据内容
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean writeToFile(String file, byte[] data) {
		try {
			org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(file), data);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("写文件出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将数据写入文件，文件不存在时自动创建
	 * @param file 文件对象或文件路径
	 * @param data 写入的数据内容
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean writeToFile(String file, String data) {
		return writeToFile(file, data, "GBK");
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将数据写入文件，文件不存在时自动创建
	 * @param file 文件对象或文件路径
	 * @param data 写入的数据内容
	 * @param encoding 文件编码，空值时使用GBK
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean writeToFile(String file, String data, String encoding) {
		try {
			if (encoding == null || "".equals(encoding)) {
				encoding = "GBK";
			}
			org.apache.commons.io.FileUtils.writeStringToFile(new File(file), data, encoding);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("写文件出错");
		}
		return false;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 创建新文件，父目录不存在时先创建目录
	 * @param filePathName 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean createNewFile(String filePathName) {
		String filePath = FileHelper.getFullPath(filePathName);

		if (!FileHelper.exists(filePath)) {
			if (!createDirectory(filePath)) {
				return false;
			}
		}

		try {
			File file = new File(filePathName);
			return file.createNewFile();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("创建文件出错");
			return false;
		}
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 判断文件或目录是否存在
	 * @param filePath 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean exists(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 判断路径是否为文件
	 * @param filePath 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean isFile(String filePath) {
		File file = new File(filePath);
		return file.isFile();
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 判断路径是否为目录
	 * @param filePath 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean isDirectory(String filePath) {
		File file = new File(filePath);
		return file.isDirectory();
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 重命名文件或移动文件到新路径
	 * @param srcFile 文件对象或文件路径
	 * @param destFile 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
	public static boolean renameTo(String srcFile, String destFile) {
		File file = new File(srcFile);
		return file.renameTo(new File(destFile));
	}

	  
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 获取文件后缀并转为大写
	 * @param fileName 文件对象或文件路径
	 * @return
	 * 返回值: String
	 * 返回值说明: 处理后的字符串
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
    public String getFileSuffix(String fileName) throws Exception {  
        return fileName.substring(fileName.lastIndexOf(".") + 1,  
                fileName.length()).toUpperCase();  
    }  
  
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 按路径逐级创建多级目录
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: void
	 * 返回值说明: 方法无返回值，会直接对文件或目录产生副作用
	 */
    public void createMultilevelDir(String path) {  
        try {  
            StringTokenizer st = new StringTokenizer(path, "/");  
            String path1 = st.nextToken() + "/";  
            String path2 = path1;  
            while (st.hasMoreTokens()) {  
  
                path1 = st.nextToken() + "/";  
                path2 += path1;  
                File inbox = new File(path2);  
                if (!inbox.exists())  
                    inbox.mkdir();  
  
            }  
        } catch (Exception e) {  
            System.out.println("目录创建失败" + e);  
            e.printStackTrace();  
        }  
  
    }  
  
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 递归删除文件或目录
	 * @param dirpath 目录对象或目录路径
	 * @return
	 * 返回值: void
	 * 返回值说明: 方法无返回值，会直接对文件或目录产生副作用
	 */
    public void deleteAll(String dirpath) {  
        if (dirpath == null) {  
            System.out.println("目录为空");  
        } else {  
            File path = new File(dirpath);  
            try {  
                if (!path.exists())  
                    return;
                if (path.isFile())
                {  
                    path.delete();  
                    return;  
                }  
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {  
                    deleteAll(files[i].getAbsolutePath());  
                }  
                path.delete();  
  
            } catch (Exception e) {  
                System.out.println("文件/目录 删除失败" + e);  
                e.printStackTrace();  
            }  
        }  
    }  
  
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 重命名文件或目录
	 * @param oldPath 文件对象或文件路径
	 * @param newPath 文件对象或文件路径
	 * @return
	 * 返回值: void
	 * 返回值说明: 方法无返回值，会直接对文件或目录产生副作用
	 */
    public void renameDir(String oldPath, String newPath) {  
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        try {  
            boolean success = oldFile.renameTo(newFile);
            if (!success) {  
                System.out.println("重命名失败");  
            } else {  
                System.out.println("重命名成功");  
            }  
        } catch (RuntimeException e) {  
            e.printStackTrace();  
        }  
  
    }  
  
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 创建目录
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
    public static boolean newDir(String path) throws Exception {  
        File file = new File(path);  
        return file.mkdirs();
    }  
      
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 递归删除目录及其内容
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
    public static boolean deleteDir(String path) throws Exception {  
        File file = new File(path);  
        if (!file.exists())  
            return false;
        if (file.isFile())
        {  
            file.delete();  
            return false;  
        }  
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {  
            deleteDir(files[i].getAbsolutePath());  
        }  
        file.delete();  
          
        return file.delete();
    }  
  
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 将目录重命名到新路径
	 * @param path 文件对象或文件路径
	 * @param newPath 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
    public static boolean updateDir(String path, String newPath) throws Exception {  
        File file = new File(path);  
        File newFile = new File(newPath);  
        return file.renameTo(newFile);  
    }
    

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 删除文件夹及其内容
	 * @param folderPath 目录对象或目录路径
	 * @return
	 * 返回值: void
	 * 返回值说明: 方法无返回值，会直接对文件或目录产生副作用
	 */
    public static void delFolder(String folderPath) {  
        try {  
            delAllFile(folderPath);
            String filePath = folderPath;  
            filePath = filePath.toString();  
            java.io.File myFilePath = new java.io.File(filePath);  
            myFilePath.delete();
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 删除指定文件夹下的全部文件
	 * @param path 文件对象或文件路径
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-处理成功，false-处理失败
	 */
    public static boolean delAllFile(String path) {  
        boolean flag = false;  
        File file = new File(path);  
        if (!file.exists()) {  
            return flag;  
        }  
        if (!file.isDirectory()) {  
            return flag;  
        }  
        String[] tempList = file.list();  
        File temp = null;  
        for (int i = 0; i < tempList.length; i++) {  
            if (path.endsWith(File.separator)) {  
                temp = new File(path + tempList[i]);  
            } else {  
                temp = new File(path + File.separator + tempList[i]);  
            }  
            if (temp.isFile()) {  
                temp.delete();  
            }  
            if (temp.isDirectory()) {  
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;  
            }  
        }  
        return flag;  
    }  
  
    
    
      
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:18
	 * 说明: 本地验证文件目录操作
	 * @param d 方法参数
	 * @return
	 * 返回值: void
	 * 返回值说明: 方法无返回值，会直接对文件或目录产生副作用
	 * @throws Exception 文件读写、路径处理或目录操作失败时抛出异常
	 */
    public static void main(String d[]) throws Exception{  

        updateDir("D:\\TOOLS\\Tomcat 6.0\\webapps\\BCCCSM\\nationalExperiment/22222", "D:\\TOOLS\\Tomcat 6.0\\webapps\\BCCCSM\\nationalExperiment/224222");  
    }  
      
}
