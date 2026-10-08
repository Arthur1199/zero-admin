package com.zero.common.xss;

import com.zero.common.utils.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 自定义xss校验注解实现
 */
public class XssValidator implements ConstraintValidator<Xss, String>
{
    /**
     * HTML_PATTERN字段
     */
    private static final String HTML_PATTERN = "<(\\S*?)[^>]*>.*?|<.*? />";
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 判断Valid
     * @param value 值参数
     * @param constraintValidatorContext constraintValidatorContext参数
     * @return
     * 返回值: boolean
     * 返回值说明：判断Valid
     */

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        return !containsHtml(value);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: containsHTML
     * @param value 值参数
     * @return
     * 返回值: boolean
     * 返回值说明：containsHTML
     */

    public static boolean containsHtml(String value)
    {
        StringBuilder sHtml = new StringBuilder();
        Pattern pattern = Pattern.compile(HTML_PATTERN);
        Matcher matcher = pattern.matcher(value);
        while (matcher.find())
        {
            sHtml.append(matcher.group());
        }
        return pattern.matcher(sHtml).matches();
    }
}