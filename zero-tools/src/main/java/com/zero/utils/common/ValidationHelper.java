package com.zero.utils.common;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 22:12
 * 说明: 通用校验工具类，集中处理账号、手机号、邮箱、身份证、URL、IP、邮编、银行卡和生日年龄校验
 */
public final class ValidationHelper {

    /**
     * 正则表达式：验证用户名，字母开头，6到20位
     */
    public static final String REGEX_USERNAME_FIVE = "^[a-zA-Z][a-zA-Z\\d]{5,19}$";

    /**
     * 正则表达式：验证用户名，数字或字母，6到15位
     */
    public static final String REGEX_USERNAME_SIX = "^[a-zA-Z0-9]{6,15}$";

    /**
     * 正则表达式：验证密码，数字或字母，6到16位
     */
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";

    /**
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[189])\\d{8}$";

    /**
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    /**
     * 正则表达式：验证汉字，允许中文姓名中间点
     */
    public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5·]+$";

    /**
     * 正则表达式：验证英文字母
     */
    public static final String REGEX_ENGLISH = "^[a-zA-Z]+$";

    /**
     * 正则表达式：验证数字
     */
    public static final String REGEX_NUMBER = "^[0-9]+$";

    /**
     * 正则表达式：验证身份证基础格式
     */
    public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

    /**
     * 正则表达式：验证URL
     */
    public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

    /**
     * 正则表达式：验证IP地址
     */
    public static final String REGEX_IP_ADDR = "([1-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]).(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$";

    /**
     * 正则表达式：验证手机所在国家code码
     */
    public static final String REGEX_PHONE_CODE = "^[+][0-9]+$";

    /**
     * 区号正则
     */
    public static final Pattern CODE_PATTERN = Pattern.compile("^0\\d{2,4}$");

    /**
     * 邮编正则
     */
    public static final Pattern POSTCODE_PATTERN = Pattern.compile("^\\d{6}$");

    /**
     * 银行卡号正则
     */
    public static final Pattern BANK_CARD_PATTERN = Pattern.compile("^\\d{16,30}$");

    /**
     * 图片路径正则，保留历史常量
     */
    public static final String ICON_REGEXP = "^(/{0,1}//w){1,}//.(gif|dmp|png|jpg)$|^//w{1,}//.(gif|dmp|png|jpg)$";

    /**
     * 邮箱正则，保留历史常量
     */
    public static final String EMAIL_REGEXP = "(?://w[-._//w]*//w@//w[-._//w]*//w//.//w{2,3}$)";

    /**
     * URL正则，保留历史常量
     */
    public static final String URL_REGEXP = "(//w+)://([^/:]+)(://d*)?([^#//s]*)";

    /**
     * HTTP/HTTPS/FTP正则，保留历史常量
     */
    public static final String HTTP_REGEXP = "(http|https|ftp)://([^/:]+)(://d*)?([^#//s]*)";

    /**
     * 横线日期正则，保留历史常量
     */
    public static final String DATE_BARS_REGEXP = "^((((19){1}|(20){1})\\d{2})|\\d{2})-[0,1]?\\d{1}-[0-3]?\\d{1}$";

    /**
     * 斜线日期正则，保留历史常量
     */
    public static final String DATE_SLASH_REGEXP = "^[0-9]{4}/(((0[13578]|(10|12))/(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)/(0[1-9]|[1-2][0-9]|30)))$";

    /**
     * 电话正则，保留历史常量
     */
    public static final String PHONE_REGEXP = "^(?:0[0-9]{2,3}[-//s]{1}|//(0[0-9]{2,4}//))[0-9]{6,8}$|^[1-9]{1}[0-9]{5,7}$|^[1-9]{1}[0-9]{10}$";

    /**
     * 身份证正则，保留历史常量
     */
    public static final String ID_CARD_REGEXP = "^//d{10}|//d{13}|//d{15}|//d{18}$";

    /**
     * 邮编正则，保留历史常量
     */
    public static final String ZIP_REGEXP = "^[0-9]{6}$";

    /**
     * 非特殊字符正则，保留历史常量
     */
    public static final String NON_SPECIAL_CHAR_REGEXP = "^[^'/";

    /**
     * 非负整数正则
     */
    public static final String NON_NEGATIVE_INTEGERS_REGEXP = "^//d+$";

    /**
     * 不含零的非负整数正则
     */
    public static final String NON_ZERO_NEGATIVE_INTEGERS_REGEXP = "^[1-9]+//d*$";

    /**
     * 正整数正则
     */
    public static final String POSITIVE_INTEGER_REGEXP = "^[0-9]*[1-9][0-9]*$";

    /**
     * 非正整数正则
     */
    public static final String NON_POSITIVE_INTEGERS_REGEXP = "^((-//d+)|(0+))$";

    /**
     * 负整数正则
     */
    public static final String NEGATIVE_INTEGERS_REGEXP = "^-[0-9]*[1-9][0-9]*$";

    /**
     * 整数正则
     */
    public static final String INTEGER_REGEXP = "^-?//d+$";

    /**
     * 非负浮点数正则
     */
    public static final String NON_NEGATIVE_RATIONAL_NUMBERS_REGEXP = "^//d+(//.//d+)?$";

    /**
     * 正浮点数正则
     */
    public static final String POSITIVE_RATIONAL_NUMBERS_REGEXP = "^(([0-9]+//.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*//.[0-9]+)|([0-9]*[1-9][0-9]*))$";

    /**
     * 非正浮点数正则
     */
    public static final String NON_POSITIVE_RATIONAL_NUMBERS_REGEXP = "^((-//d+(//.//d+)?)|(0+(//.0+)?))$";

    /**
     * 负浮点数正则
     */
    public static final String NEGATIVE_RATIONAL_NUMBERS_REGEXP = "^(-(([0-9]+//.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*//.[0-9]+)|([0-9]*[1-9][0-9]*)))$";

    /**
     * 浮点数正则
     */
    public static final String RATIONAL_NUMBERS_REGEXP = "^(-?//d+)(//.//d+)?$";

    /**
     * 字母正则
     */
    public static final String LETTER_REGEXP = "^[A-Za-z]+$";

    /**
     * 大写字母正则
     */
    public static final String UPWARD_LETTER_REGEXP = "^[A-Z]+$";

    /**
     * 小写字母正则
     */
    public static final String LOWER_LETTER_REGEXP = "^[a-z]+$";

    /**
     * 字母数字正则
     */
    public static final String LETTER_NUMBER_REGEXP = "^[A-Za-z0-9]+$";

    /**
     * 字母数字下划线正则
     */
    public static final String LETTER_NUMBER_UNDERLINE_REGEXP = "^//w+$";

    private ValidationHelper() {
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验用户名，字母开头，6到20位
     * @param username 用户名
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isUsernameFiveToTen(String username) {
        return patternCheck(username, REGEX_USERNAME_FIVE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验用户名，数字或字母，6到15位
     * @param username 用户名
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isUsernameSixToTen(String username) {
        return patternCheck(username, REGEX_USERNAME_SIX);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验密码，数字或字母，6到16位
     * @param password 密码
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isPassword(String password) {
        return patternCheck(password, REGEX_PASSWORD);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验手机号
     * @param mobile 手机号
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isMobile(String mobile) {
        return patternCheck(mobile, REGEX_MOBILE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验邮箱地址
     * @param email 邮箱地址
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isEmail(String email) {
        return patternCheck(email, REGEX_EMAIL);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验中文，允许中文姓名中间点
     * @param chinese 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-全部为中文，false-格式错误或为空
     */
    public static boolean isChinese(String chinese) {
        return patternCheck(chinese, REGEX_CHINESE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验英文字母
     * @param english 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-全部为英文字母，false-格式错误或为空
     */
    public static boolean isEnglish(String english) {
        return patternCheck(english, REGEX_ENGLISH);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验数字
     * @param number 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-全部为数字，false-格式错误或为空
     */
    public static boolean isNumber(String number) {
        return patternCheck(number, REGEX_NUMBER);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验身份证基础格式
     * @param idCard 身份证号
     * @return
     * 返回值: boolean
     * 返回值说明: true-15位或18位数字，false-格式错误或为空
     */
    public static boolean isIDCard(String idCard) {
        return patternCheck(idCard, REGEX_ID_CARD);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验HTTP或HTTPS URL
     * @param url URL字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isUrl(String url) {
        return patternCheck(url, REGEX_URL);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验IP地址
     * @param ipAddr IP地址
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isIPAddr(String ipAddr) {
        return patternCheck(ipAddr, REGEX_IP_ADDR);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验手机号国家code，例如+86
     * @param code 国家code
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isPhoneCode(String code) {
        return patternCheck(code, REGEX_PHONE_CODE);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 根据国家代码和手机号判断手机号是否有效
     * @param phoneNumber 手机号
     * @param countryCode 国家代码，不包含加号
     * @return
     * 返回值: boolean
     * 返回值说明: true-手机号有效，false-参数为空、格式错误或号码无效
     */
    public static boolean checkPhoneNumber(String phoneNumber, String countryCode) {
        if (StringHelper.isEmpty(phoneNumber) || StringHelper.isEmpty(countryCode)) {
            return false;
        }
        try {
            int ccode = Integer.parseInt(countryCode);
            long phone = Long.parseLong(phoneNumber);
            Phonenumber.PhoneNumber pn = new Phonenumber.PhoneNumber();
            pn.setCountryCode(ccode);
            pn.setNationalNumber(phone);
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            return phoneUtil.isValidNumber(pn);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验邮箱地址，保留历史validateEmail方法名
     * @param str 邮箱地址
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean validateEmail(String str) {
        String pattern = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        return patternCheck(str, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验手机号，保留历史validateMoblie方法名和拼写
     * @param str 手机号
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean validateMoblie(String str) {
        return patternCheck(str, "^(13|14|15|17|18)[0-9]{9}$");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 验证区号是否有效
     * @param code 区号
     * @return
     * 返回值: boolean
     * 返回值说明: true-区号格式正确，false-格式错误或为空
     */
    public static boolean validateCode(String code) {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 验证邮政编码是否有效
     * @param postcode 邮政编码
     * @return
     * 返回值: boolean
     * 返回值说明: true-邮编格式正确，false-格式错误或为空
     */
    public static boolean validatePostcode(String postcode) {
        return postcode != null && POSTCODE_PATTERN.matcher(postcode).matches();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 验证银行卡号是否有效
     * @param bankCardNumber 银行卡号
     * @return
     * 返回值: boolean
     * 返回值说明: true-银行卡号格式正确，false-格式错误或为空
     */
    public static boolean validateBankCardNumber(String bankCardNumber) {
        return bankCardNumber != null && BANK_CARD_PATTERN.matcher(bankCardNumber).matches();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过身份证获取生日
     * @param idNumber 身份证号，支持15位或18位
     * @return
     * 返回值: String
     * 返回值说明: yyyy-MM-dd格式生日；身份证长度不支持时返回空字符串
     */
    public static String getBirthdayByIdNumber(String idNumber) {
        if (idNumber == null) {
            return "";
        }
        if (idNumber.length() == 15) {
            return "19" + idNumber.substring(6, 8) + "-" + idNumber.substring(8, 10) + "-" + idNumber.substring(10, 12);
        } else if (idNumber.length() == 18) {
            return idNumber.substring(6, 10) + "-" + idNumber.substring(10, 12) + "-" + idNumber.substring(12, 14);
        }
        return "";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过身份证获取性别
     * @param idNumber 身份证号，支持15位或18位
     * @return
     * 返回值: Integer
     * 返回值说明: 0-保密，1-男，2-女；身份证长度不支持时返回0
     */
    public static Integer getGenderByIdNumber(String idNumber) {
        if (idNumber == null) {
            return 0;
        }
        if (idNumber.length() == 15) {
            return Integer.parseInt(String.valueOf(idNumber.charAt(14))) % 2 == 0 ? 2 : 1;
        } else if (idNumber.length() == 18) {
            return Integer.parseInt(String.valueOf(idNumber.charAt(16))) % 2 == 0 ? 2 : 1;
        }
        return 0;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过身份证获取年龄
     * @param idNumber 身份证号
     * @param isNominalAge true-按元旦算年龄，false-按生日当天算年龄
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；身份证无法解析生日时返回0
     */
    public static Integer getAgeByIdNumber(String idNumber, boolean isNominalAge) {
        String birthString = getBirthdayByIdNumber(idNumber);
        if (StringHelper.isEmpty(birthString)) {
            return 0;
        }
        return getAgeByBirthString(birthString, isNominalAge);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过生日日期获取年龄
     * @param birthDate 生日日期
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；入参为空时返回0
     */
    public static Integer getAgeByBirthDate(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return getAgeByBirthString(new SimpleDateFormat("yyyy-MM-dd").format(birthDate));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过生日字符串获取年龄，默认格式yyyy-MM-dd
     * @param birthString 生日字符串
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；生日为空或解析失败时返回0
     */
    public static Integer getAgeByBirthString(String birthString) {
        return getAgeByBirthString(birthString, "yyyy-MM-dd");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过生日字符串获取年龄，默认格式yyyy-MM-dd
     * @param birthString 生日字符串
     * @param isNominalAge true-按元旦算年龄，false-按生日当天算年龄
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；生日为空或解析失败时返回0
     */
    public static Integer getAgeByBirthString(String birthString, boolean isNominalAge) {
        return getAgeByBirthString(birthString, "yyyy-MM-dd", isNominalAge);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过生日字符串获取年龄
     * @param birthString 生日字符串
     * @param format 日期格式，为空时默认yyyy-MM-dd
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；生日为空或解析失败时返回0
     */
    public static Integer getAgeByBirthString(String birthString, String format) {
        return getAgeByBirthString(birthString, format, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 通过生日字符串获取年龄
     * @param birthString 生日字符串
     * @param format 日期格式，为空时默认yyyy-MM-dd
     * @param isNominalAge true-按元旦算年龄，false-按生日当天算年龄
     * @return
     * 返回值: Integer
     * 返回值说明: 年龄；生日为空或解析失败时返回0
     */
    public static Integer getAgeByBirthString(String birthString, String format, boolean isNominalAge) {
        int age = 0;
        if (StringHelper.isEmpty(birthString)) {
            return age;
        }
        if (StringHelper.isEmpty(format)) {
            format = "yyyy-MM-dd";
        }
        try {
            Calendar birthday = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            birthday.setTime(sdf.parse(birthString));
            age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
            if (!isNominalAge && (today.get(Calendar.MONTH) < birthday.get(Calendar.MONTH)
                    || (today.get(Calendar.MONTH) == birthday.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < birthday.get(Calendar.DAY_OF_MONTH)))) {
                age = age - 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 使用大小写敏感的正则表达式校验字符串
     * @param str 待校验字符串
     * @param regexp 正则表达式
     * @return
     * 返回值: boolean
     * 返回值说明: true-匹配，false-不匹配、入参为空或正则为空
     */
    public static boolean isHardRegexpValidate(String str, String regexp) {
        return patternCheck(str, regexp);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验手机号，保留历史isPhoneNum方法名
     * @param phoneNum 手机号
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isPhoneNum(String phoneNum) {
        String pattern = "^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$";
        return patternCheck(phoneNum, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验身份证号基础格式，保留历史isIdCard方法名
     * @param idCardStr 身份证号
     * @return
     * 返回值: boolean
     * 返回值说明: true-符合15位或18位基础格式，false-格式错误或为空
     */
    public static boolean isIdCard(String idCardStr) {
        String pattern = "(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])";
        return patternCheck(idCardStr, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验HTTP或HTTPS地址，保留历史isURL方法名
     * @param urlStr URL字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-格式正确，false-格式错误或为空
     */
    public static boolean isURL(String urlStr) {
        return patternCheck(urlStr, REGEX_URL);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验IP分段数字，取值范围0到255
     * @param urlStr 待校验字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-0到255之间的数字，false-格式错误或为空
     */
    public static boolean isIp(String urlStr) {
        String pattern = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        return patternCheck(urlStr, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 校验金额格式，最多保留两位小数
     * @param amount 金额字符串
     * @return
     * 返回值: boolean
     * 返回值说明: true-金额格式正确，false-格式错误或为空
     */
    public static boolean isAmount(String amount) {
        String pattern = "^\\d+(|\\.\\d{1,2})$";
        return patternCheck(amount, pattern);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 22:12
     * 说明: 正则校验公共入口
     * @param sourceStr 待校验字符串
     * @param pattern 正则表达式
     * @return
     * 返回值: boolean
     * 返回值说明: true-匹配，false-不匹配、字符串为空或正则为空
     */
    private static boolean patternCheck(String sourceStr, String pattern) {
        if (sourceStr == null || pattern == null) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(sourceStr);
        return m.matches();
    }
}
