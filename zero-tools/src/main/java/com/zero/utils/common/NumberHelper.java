package com.zero.utils.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 22:05
 * 说明: 数字格式化、高精度计算、缩放和随机码工具类
 */
public class NumberHelper {

    /**
     * 默认除法运算保留小数位
     */
    private static final int DEF_DIV_SCALE = 10;

    /**
     * 纯数字随机码基数
     */
    private static final int RANDOM_BASE = 10;

    /**
     * 复杂验证码字符池，排除容易混淆的0、1、O、I
     */
    private static final String[] CODE_STR = {"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
            "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private NumberHelper() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 将整数拆解为包含哪些2的N次方位
     * @param i 待拆解数值
     * @return
     * 返回值: int[]
     * 返回值说明: 命中的位序号数组，从0开始
     */
    public static int[] getWhich(long i) {
        int exp = Math.getExponent(i);
        if (i == (1 << (exp + 1)) - 1) {
            exp = exp + 1;
        }
        int[] num = new int[exp];
        int x = exp - 1;
        for (int n = 0; (1 << n) < i + 1; n++) {
            if ((1 << (n + 1)) > i && (1 << n) < (i + 1)) {
                num[x] = n;
                i -= 1 << n;
                n = 0;
                x--;
            }
        }
        return num;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 将double数字格式化为带千分位和4位小数的字符串
     * @param number 需要格式化的数字
     * @return
     * 返回值: String
     * 返回值说明: 格式化后的数字字符串
     */
    public static String numberFormat(Double number) {
        DecimalFormat df = new DecimalFormat("###,###,###,###,###.0000");
        return df.format(number);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 判断字符串是否为正数
     * @param target 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-可转换为正数，false-不可转换为正数
     */
    public static boolean validPositiveInteger(String target) {
        if (target == null) {
            return false;
        }
        Matcher mer = Pattern.compile("^[+]?\\d+(.\\d+)?$").matcher(target);
        return mer.find();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确加法运算
     * @param v1 被加数
     * @param v2 加数
     * @return
     * 返回值: double
     * 返回值说明: 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确加法运算
     * @param v1 被加数字符串
     * @param v2 加数字符串
     * @return
     * 返回值: double
     * 返回值说明: 两个字符串数字的和
     */
    public static double add(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确加法后按四舍五入保留指定小数位
     * @param v1 被加数
     * @param v2 加数
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 四舍五入后的加法结果
     */
    public static double add(double v1, double v2, int scale) {
        checkScale(scale);
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确减法运算
     * @param v1 被减数
     * @param v2 减数
     * @return
     * 返回值: double
     * 返回值说明: 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确乘法运算
     * @param v1 被乘数
     * @param v2 乘数
     * @return
     * 返回值: double
     * 返回值说明: 两个参数的乘积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确除法运算，默认向下截取10位小数
     * @param v1 被除数
     * @param v2 除数，小于等于0时返回0
     * @return
     * 返回值: double
     * 返回值说明: 两个参数的商
     */
    public static double div(double v1, double v2) {
        if (v2 <= 0) {
            return 0;
        }
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确除法运算，按指定小数位向下截取
     * @param v1 被除数
     * @param v2 除数，小于等于0时返回0
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 向下截取后的商
     */
    public static double div(double v1, double v2, int scale) {
        if (v2 <= 0) {
            return 0;
        }
        checkScale(scale);
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.DOWN).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确除法运算，默认四舍五入保留10位小数
     * @param v1 被除数
     * @param v2 除数
     * @return
     * 返回值: double
     * 返回值说明: 四舍五入后的商
     */
    public static double divHalfUp(double v1, double v2) {
        return divHalfUp(v1, v2, DEF_DIV_SCALE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确除法运算，按指定小数位四舍五入
     * @param v1 被除数
     * @param v2 除数
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 四舍五入后的商
     */
    public static double divHalfUp(double v1, double v2, int scale) {
        checkScale(scale);
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 非四舍五入取整处理
     * @param v 需要处理的数字
     * @return
     * 返回值: int
     * 返回值说明: 向下截取后的整数
     */
    public static int roundDown(double v) {
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, 0, RoundingMode.DOWN).intValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定小数位向下截取数字
     * @param v 需要处理的数字
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 向下截取后的数字
     */
    public static double roundDown(double v, int scale) {
        checkScale(scale);
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.DOWN).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 四舍五入取整处理
     * @param v 需要处理的数字
     * @return
     * 返回值: int
     * 返回值说明: 向上取整后的整数
     */
    public static int roundUp(double v) {
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, 0, RoundingMode.UP).intValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 提供精确的小数位四舍五入处理
     * @param v 需要处理的数字
     * @param scale 小数点后保留几位
     * @return
     * 返回值: double
     * 返回值说明: 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        checkScale(scale);
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 四舍五入保留两位小数
     * @param num 数字
     * @return
     * 返回值: String
     * 返回值说明: 保留两位小数的数字字符串
     */
    public static String format(double num) {
        return format(num, "0.00");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定小数位格式化数字
     * @param num 数字
     * @param digits 小数位数
     * @return
     * 返回值: String
     * 返回值说明: 格式化后的数字字符串
     */
    public static String format(double num, int digits) {
        String pattern = "0";
        if (digits > 0) {
            pattern += "." + createStr("0", digits, "");
        }
        return format(num, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定格式格式化数字
     * @param num 数字
     * @param pattern 数字格式，为空时使用默认格式
     * @return
     * 返回值: String
     * 返回值说明: 格式化后的数字字符串
     */
    public static String format(double num, String pattern) {
        NumberFormat fmt;
        if (pattern != null && !pattern.isEmpty()) {
            fmt = new DecimalFormat(pattern);
        } else {
            fmt = new DecimalFormat();
        }
        return fmt.format(num);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 小数位四舍五入
     * @param value 需要处理的数字
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 四舍五入后的数字
     */
    public static double forScale(double value, int scale) {
        checkScale(scale);
        BigDecimal b = new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
        return b.doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 数组内每个数字按指定小数位四舍五入
     * @param value 需要处理的数字数组
     * @param scale 保留小数位数
     * @return
     * 返回值: double[]
     * 返回值说明: 原数组对象，数组元素已四舍五入
     */
    public static double[] forScale(double[] value, int scale) {
        checkScale(scale);
        if (value != null && value.length > 0) {
            for (int i = 0; i < value.length; i++) {
                value[i] = forScale(value[i], scale);
            }
        }
        return value;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 统计数字的小数位数，末尾0不计入
     * @param number 需要统计的数字
     * @return
     * 返回值: int
     * 返回值说明: 小数位数
     */
    public static int getNumberDecimalDigits(double number) {
        String moneyStr = number + "";
        String[] num = moneyStr.split("\\.");
        if (num.length == 2) {
            for (;;) {
                if (num[1].endsWith("0")) {
                    num[1] = num[1].substring(0, num[1].length() - 1);
                } else {
                    break;
                }
            }
            return num[1].length();
        }
        return 0;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: double转float
     * @param v 需要转换的数字
     * @return
     * 返回值: float
     * 返回值说明: 转换后的float数字
     */
    public static float convertsToFloat(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.floatValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: double转int，不进行四舍五入
     * @param v 需要转换的数字
     * @return
     * 返回值: int
     * 返回值说明: 转换后的int数字
     */
    public static int convertsToInt(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.intValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: double转long，不进行四舍五入
     * @param v 需要转换的数字
     * @return
     * 返回值: long
     * 返回值说明: 转换后的long数字
     */
    public static long convertsToLong(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.longValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 返回两个数字中的较大值
     * @param v1 第一个数字
     * @param v2 第二个数字
     * @return
     * 返回值: double
     * 返回值说明: 两个数字中的较大值
     */
    public static double returnMax(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.max(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 返回两个数字中的较小值
     * @param v1 第一个数字
     * @param v2 第二个数字
     * @return
     * 返回值: double
     * 返回值说明: 两个数字中的较小值
     */
    public static double returnMin(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.min(b2).doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 精确比较两个数字大小
     * @param v1 第一个数字
     * @param v2 第二个数字
     * @return
     * 返回值: int
     * 返回值说明: 0-相等，1-v1大于v2，-1-v1小于v2
     */
    public static int compareTo(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.compareTo(b2);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定小数位向下截取数字
     * @param v1 需要处理的数字
     * @param scale 保留小数位数
     * @return
     * 返回值: double
     * 返回值说明: 向下截取后的数字
     */
    public static double subDouble(double v1, int scale) {
        checkScale(scale);
        BigDecimal bd = new BigDecimal(Double.toString(v1)).setScale(scale, RoundingMode.DOWN);
        return bd.doubleValue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定小数位格式化数字，不足位数补0
     * @param v 需要格式化的数字
     * @param scale 保留小数位数
     * @return
     * 返回值: String
     * 返回值说明: 格式化后的数字字符串
     */
    public static String roundByScale(double v, int scale) {
        checkScale(scale);
        if (scale == 0) {
            return new DecimalFormat("0").format(v);
        }
        return new DecimalFormat("0." + "0".repeat(Math.max(0, scale))).format(v);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 求浮点数的权重
     * @param number 需要计算权重的数字
     * @return
     * 返回值: double
     * 返回值说明: 数字权重，0返回1
     */
    public static double weight(double number) {
        if (number == 0) {
            return 1;
        }
        double e = Math.log10(Math.abs(number));
        int n = Double.valueOf(Math.floor(e)).intValue();
        double weight = 1;
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                weight *= 10;
            }
        } else {
            for (int i = 0; i > n; i--) {
                weight /= 10;
            }
        }
        return weight;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 根据缩放倍率返回中文单位
     * @param scale 缩放倍率
     * @return
     * 返回值: String
     * 返回值说明: 中文单位名称；超出范围时返回无量
     */
    public static String unit(double scale) {
        if (scale == 1 || scale == 0) {
            return "";
        }
        String[] units = new String[] { "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿", "百亿", "千亿", "兆" };
        String[] units2 = new String[] { "十分", "百分", "千分", "万分", "十万分", "百万分", "千万分" };
        double e = Math.log10(scale);
        int position = Double.valueOf(Math.ceil(e)).intValue();
        if (position >= 1 && position <= units.length) {
            return units[position - 1];
        } else if (position <= -1 && -position <= units2.length) {
            return units2[-position - 1];
        }
        return "无量";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 根据数字大小计算缩放比例
     * @param num 需要缩放的数字
     * @return
     * 返回值: double
     * 返回值说明: 缩放比例
     */
    public static double scale(double num) {
        double absValue = Math.abs(num);
        if (absValue < 10000 && absValue >= 1) {
            return 1;
        } else if (absValue < 1 && absValue > 0.0001) {
            return 1;
        }
        return weight(num) / 10;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按缩放比例转换并格式化数字
     * @param num 原始数字
     * @param scale 缩放比例
     * 返回值: double
     * 返回值说明: 缩放后的数字，比例为1时保留4位，否则保留2位
     */
    public static double scaleNumber(double num, double scale) {
        DecimalFormat df;
        if (scale == 1) {
            df = new DecimalFormat("#.0000");
        } else {
            df = new DecimalFormat("#.00");
        }
        double scaledNum = num / scale;
        return Double.parseDouble(df.format(scaledNum));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 产生指定长度的纯数字随机码，保留历史拼写兼容调用
     * @param n 随机码位数，必须大于0
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的纯数字随机码
     */
    public static String ramdomNumber(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive !");
        }
        return getRandomStr(n);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 将数字缩放1万倍
     * @param number 原始数字
     * 返回值: double
     * 返回值说明: 缩放后的数字，保留正负号
     */
    public static double changeTo(double number) {
        boolean flag = number < 0;
        double value = Math.abs(number) / 10000.0;
        if (flag) {
            value = Double.parseDouble("-" + value);
        }
        return value;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按指定缩放比例转换为固定小数位字符串
     * @param number 原始数字
     * @param scale 缩放比例
     * @param points 小数位数
     * @return
     * 返回值: String
     * 返回值说明: 缩放并格式化后的数字字符串
     */
    public static String scaleNumberToStr(double number, double scale, int points) {
        boolean flag = number < 0;
        number = Math.abs(number);
        DecimalFormat nbf3 = (DecimalFormat) NumberFormat.getInstance();
        nbf3.setGroupingUsed(false);
        nbf3.setMinimumFractionDigits(points);
        nbf3.setMaximumFractionDigits(points);
        double scaledNum = number / scale;
        String result = nbf3.format(scaledNum);
        if (flag) {
            result = "-" + result;
        }
        return result;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 产生指定长度的纯数字随机码
     * @param length 随机码位数
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的纯数字随机码
     */
    public static String getRandomStr(int length) {
        Random random = new Random();
        StringBuilder randStr = new StringBuilder();
        for (int i = 0; i < length; i++) {
            randStr.append(random.nextInt(RANDOM_BASE));
        }
        return randStr.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 生成数字加大写字母随机码
     * @param len 随机码位数
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的数字加大写字母随机码
     */
    public static String generateChatAndNumberIdentifyCode(int len) {
        char[] identifyStr = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        int min = 0;
        int magnum = identifyStr.length;
        StringBuilder codeStr = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int num = (int) ((magnum - min) * Math.random() + min);
            codeStr.append(identifyStr[num]);
        }
        return codeStr.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 生成纯大写字母随机码
     * @param len 随机码位数
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的大写字母随机码
     */
    public static String generateIdentifyCode(int len) {
        char[] identifyStr = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        int min = 0;
        int magnum = identifyStr.length;
        StringBuilder codeStr = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int num = (int) ((magnum - min) * Math.random() + min);
            codeStr.append(identifyStr[num]);
        }
        return codeStr.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 生成指定范围内的随机整数，包含起止边界
     * @param start 起始值
     * @param end 结束值
     * @return
     * 返回值: int
     * 返回值说明: start到end之间的随机整数
     */
    public static int getRandomNumber(int start, int end) {
        Random random = new Random();
        return random.nextInt(end - start + 1) + start;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按位数生成纯数字随机码
     * @param count 随机码位数
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的数字验证码或邀请码
     */
    public static String getCode(int count) {
        return getRandomStr(count);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 按位数生成数字加大写字母的复杂随机码
     * @param count 随机码位数
     * @return
     * 返回值: String
     * 返回值说明: 指定位数的复杂验证码或邀请码
     */
    public static String getComplexCode(int count) {
        StringBuilder buf = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            buf.append(CODE_STR[random.nextInt(CODE_STR.length)]);
        }
        return buf.toString();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 生成不带横线的随机UUID
     * @return
     * 返回值: String
     * 返回值说明: 32位UUID字符串
     */
    public static String randomUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 生成不带横线的随机UUID，保留旧方法拼写兼容历史调用
     * @return
     * 返回值: String
     * 返回值说明: 32位UUID字符串
     */
    public static String ramdomUUID() {
        return randomUuid();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 重复生成字符串，用于组装数字格式模板
     * @param value 字符串元素
     * @param count 生成个数
     * @param separator 间隔符号，为空时按空字符串处理
     * @return
     * 返回值: String
     * 返回值说明: 生成后的字符串
     */
    private static String createStr(String value, int count, String separator) {
        return StringHelper.createStr(value, count, separator);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:05
     * 说明: 校验小数位数不能为负数
     * @param scale 保留小数位数
     * @return
     * 返回值: void
     * 返回值说明: 小数位数非法时抛出异常
     */
    private static void checkScale(int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
    }
}
