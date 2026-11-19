package com.zero.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import com.zero.common.utils.poi.ExcelHandlerAdapter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 自定义导出Excel数据注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel
{
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出时在excel中排序
     * @return
     * 返回值: int
     * 返回值说明: 导出时在excel中排序
     */
    public int sort() default Integer.MAX_VALUE;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出到Excel中的名字.
     * @return
     * 返回值: String
     * 返回值说明: 导出到Excel中的名字
     */
    public String name() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 日期格式, 如: yyyy-MM-dd
     * @return
     * 返回值: String
     * 返回值说明: 日期格式, 如: yyyy-MM-dd
     */
    public String dateFormat() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 如果是字典类型，请设置字典的type值 (如: sys_user_sex)
     * @return
     * 返回值: String
     * 返回值说明: 如果是字典类型，请设置字典的type值 (如: sys_user_sex)
     */
    public String dictType() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取内容转表达式 (如: 0=男,1=女,2=未知)
     * @return
     * 返回值: String
     * 返回值说明: 读取内容转表达式 (如: 0=男,1=女,2=未知)
     */
    public String readConverterExp() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 分隔符，读取字符串组内容
     * @return
     * 返回值: String
     * 返回值说明: 分隔符，读取字符串组内容
     */
    public String separator() default ",";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: BigDecimal 精度 默认:-1(默认不开启BigDecimal格式化)
     * @return
     * 返回值: int
     * 返回值说明: BigDecimal 精度 默认:-1(默认不开启BigDecimal格式化)
     */
    public int scale() default -1;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: BigDecimal 舍入规则 默认:BigDecimal.ROUND_HALF_EVEN
     * @return
     * 返回值: int
     * 返回值说明: BigDecimal 舍入规则 默认:BigDecimal.ROUND_HALF_EVEN
     */
    public int roundingMode() default BigDecimal.ROUND_HALF_EVEN;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出时在excel中每个列的高度
     * @return
     * 返回值: double
     * 返回值说明: 导出时在excel中每个列的高度
     */
    public double height() default 14;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出时在excel中每个列的宽度
     * @return
     * 返回值: double
     * 返回值说明: 导出时在excel中每个列的宽度
     */
    public double width() default 16;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 文字后缀,如% 90 变成90%
     * @return
     * 返回值: String
     * 返回值说明: 文字后缀,如% 90 变成90%
     */
    public String suffix() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 当值为空时,字段的默认值
     * @return
     * 返回值: String
     * 返回值说明: 当值为空时,字段的默认值
     */
    public String defaultValue() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 提示信息
     * @return
     * 返回值: String
     * 返回值说明: 提示信息
     */
    public String prompt() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否允许内容换行
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean wrapText() default false;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 设置只能选择不能输入的列内容.
     * @return
     * 返回值: String[]
     * 返回值说明: 设置只能选择不能输入的列内容
     */
    public String[] combo() default {};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否从字典读数据到combo,默认不读取,如读取需要设置dictType注解.
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean comboReadDict() default false;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否需要纵向合并单元格,应对需求:含有list集合单元格)
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean needMerge() default false;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean isExport() default true;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 另一个类中的属性名称,支持多级获取,以小数点隔开
     * @return
     * 返回值: String
     * 返回值说明: 另一个类中的属性名称,支持多级获取,以小数点隔开
     */
    public String targetAttr() default "";

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 是否自动统计数据,在最后追加一行统计数据总和
     * @return
     * 返回值: boolean
     * 返回值说明: 符合上述判断条件时返回 true
     */
    public boolean isStatistics() default false;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出类型（0数字 1字符串 2图片）
     * @return
     * 返回值: ColumnType
     * 返回值说明: 导出类型（0数字 1字符串 2图片）
     */
    public ColumnType cellType() default ColumnType.STRING;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出列头背景颜色
     * @return
     * 返回值: IndexedColors
     * 返回值说明: 导出列头背景颜色
     */
    public IndexedColors headerBackgroundColor() default IndexedColors.GREY_50_PERCENT;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出列头字体颜色
     * @return
     * 返回值: IndexedColors
     * 返回值说明: 导出列头字体颜色
     */
    public IndexedColors headerColor() default IndexedColors.WHITE;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出单元格背景颜色
     * @return
     * 返回值: IndexedColors
     * 返回值说明: 导出单元格背景颜色
     */
    public IndexedColors backgroundColor() default IndexedColors.WHITE;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出单元格字体颜色
     * @return
     * 返回值: IndexedColors
     * 返回值说明: 导出单元格字体颜色
     */
    public IndexedColors color() default IndexedColors.BLACK;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 导出字段对齐方式
     * @return
     * 返回值: HorizontalAlignment
     * 返回值说明: 导出字段对齐方式
     */
    public HorizontalAlignment align() default HorizontalAlignment.CENTER;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 自定义数据处理器
     * @return
     * 返回值: Class<?>
     * 返回值说明: 自定义数据处理器
     */
    public Class<?> handler() default ExcelHandlerAdapter.class;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 自定义数据处理器参数
     * @return
     * 返回值: String[]
     * 返回值说明: 自定义数据处理器参数
     */
    public String[] args() default {};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 字段类型（0：导出导入；1：仅导出；2：仅导入）
     * @return
     * 返回值: Type
     * 返回值说明: 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    Type type() default Type.ALL;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 类型枚举类型
     */

    public enum Type
    {
        ALL(0), EXPORT(1), IMPORT(2);
        /**
         * 值字段
         */
        private final int value;

        Type(int value)
        {
            this.value = value;
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/14 00:58
         * 说明: 值
         * @return
         * 返回值: int
         * 返回值说明：值
         */

        public int value()
        {
            return this.value;
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 字段类型枚举字段类型
     */

    public enum ColumnType
    {
        NUMERIC(0), STRING(1), IMAGE(2), TEXT(3);
        /**
         * 值字段
         */
        private final int value;

        ColumnType(int value)
        {
            this.value = value;
        }
        /**
         *
         * 作者: Arthur 亚瑟
         * 时间: 2026/5/14 00:58
         * 说明: 值
         * @return
         * 返回值: int
         * 返回值说明：值
         */

        public int value()
        {
            return this.value;
        }
    }
}