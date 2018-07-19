package engine.android.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 身份校验器
 * 
 * @author Daimon
 * @version 3.0
 * @since 11/21/2012
 */

public final class IDCard {

    private String area;        // 所在地区

    private String date;        // 出生日期

    private String sex;         // 性别

    private String newIDCard;   // 新身份证号

    private String errorInfo;   // 出错信息

    /**
     * 1、号码的结构
         公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。
         排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
         
     * 2、地址码(前六位数）
         表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。
         
     * 3、出生日期码（第七位至十四位）
         表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。
         
     * 4、顺序码（第十五位至十七位）
         表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配给女性。
         
     * 5、校验码（第十八位数）
         （1）十七位数字本体码加权求和公式
     S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
     Ai:表示第i位置上的身份证号码数字值
     Wi:表示第i位置上的加权因子
     Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
         （2）计算模
     Y = mod(S, 11)
         （3）通过模得到对应的校验码
     Y:     0 1 2 3 4 5 6 7 8 9 10
         校验码: 1 0 X 9 8 7 6 5 4 3 2
     * 
     * 所以我们就可以大致写一个函数来校验是否正确了。
     */

    /**
     * 功能：身份证的有效验证
     * 
     * @param IDCard 身份证号
     * @return 是否有效
     */

    public boolean IDCardValidate(String IDCard) {
        String Ai = "";

        // ================ 号码的长度15位或18位 ================
        int len = IDCard.length();
        if (len != 15 && len != 18)
        {
            errorInfo = "号码长度应该为15位或18位。";
            return false;
        }
        // ======================= (end) =======================

        // ================ 除最后以外都为数字 ==================
        if (len == 18)
        {
            Ai = IDCard.substring(0, 17);
        }
        else if (len == 15)
        {
            Ai = IDCard.substring(0, 6) + "19" + IDCard.substring(6);
        }

        if (!isNumeric(Ai))
        {
            errorInfo = "15位号码都应为数字；18位号码除最后一位外，都应为数字。";
            return false;
        }
        // ======================= (end) =======================

        // ================= 出生年月是否有效 ===================
        String year = Ai.substring(6, 10);// 年
        String month = Ai.substring(10, 12);// 月
        String day = Ai.substring(12, 14);// 日
        if (!isDate(year + "-" + month + "-" + day))
        {
            errorInfo = "生日无效。";
            return false;
        }

        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (gc.get(Calendar.YEAR) - Integer.parseInt(year) > 150
            ||  gc.getTime().getTime() - sdf.parse(year + "-" + month + "-" + day).getTime() < 0)
            {
                errorInfo = "生日不在有效范围。";
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (Integer.parseInt(month) > 12 || Integer.parseInt(month) == 0)
        {
            errorInfo = "月份无效";
            return false;
        }

        if (Integer.parseInt(day) > 31 || Integer.parseInt(day) == 0)
        {
            errorInfo = "日期无效";
            return false;
        }

        date = year + "年" + month + "月" + day + "日";
        // ======================= (end) =======================

        // ================== 地区码是否有效 ====================
        Map<String, String> map = getAreaCode();
        if (!map.containsKey(Ai.substring(0, 2)))
        {
            errorInfo = "地区编码错误。";
            return false;
        }

        area = map.get(Ai.substring(0, 2));
        // ======================= (end) =======================

        // ===================== 判断性别 =======================
        if (Integer.parseInt(Ai.substring(16, 17)) % 2 == 0)
        {
            sex = "女";
        }
        else
        {
            sex = "男";
        }
        // ======================= (end) =======================

        // ================= 判断最后一位的值 ===================
        String[] ValCode = { "1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2" };
        String[] Wi = { "7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", 
                "10", "5", "8", "4", "2" };
        int sum = 0;
        for (int i = 0; i < 17; i++)
        {
            sum += Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }

        int mod = sum % 11;
        String verifyCode = ValCode[mod];
        Ai += verifyCode;

        if (len == 18)
        {
            if (!Ai.equals(IDCard))
            {
                errorInfo = "最后一位字母错误";
                return false;
            }
        }
        // ======================= (end) =======================

        newIDCard = Ai;
        return true;
    }

    /**
     * 功能：判断字符串是否为数字
     */

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 功能：判断字符串是否为日期格式
     */

    private boolean isDate(String str) {
        Pattern pattern = Pattern.compile(
                "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?" +
                "((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])" +
                "|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])" +
                "|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])" +
                "|([1-2][0-9])))))|(\\d{2}(([02468][1235679])" +
                "|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])" +
                "|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])" +
                "|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])" +
                "|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])" +
                "|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])" +
                "|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)" +
                "|(\\:([0-5]?[0-9])))))?$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 功能：设置地区编码
     */

    private Map<String, String> getAreaCode() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("11", "北京");
        map.put("12", "天津");
        map.put("13", "河北");
        map.put("14", "山西");
        map.put("15", "内蒙古");
        map.put("21", "辽宁");
        map.put("22", "吉林");
        map.put("23", "黑龙江");
        map.put("31", "上海");
        map.put("32", "江苏");
        map.put("33", "浙江");
        map.put("34", "安徽");
        map.put("35", "福建");
        map.put("36", "江西");
        map.put("37", "山东");
        map.put("41", "河南");
        map.put("42", "湖北");
        map.put("43", "湖南");
        map.put("44", "广东");
        map.put("45", "广西");
        map.put("46", "海南");
        map.put("50", "重庆");
        map.put("51", "四川");
        map.put("52", "贵州");
        map.put("53", "云南");
        map.put("54", "西藏");
        map.put("61", "陕西");
        map.put("62", "甘肃");
        map.put("63", "青海");
        map.put("64", "宁夏");
        map.put("65", "新疆");
        map.put("71", "台湾");
        map.put("81", "香港");
        map.put("82", "澳门");
        map.put("91", "国外");
        return map;
    }

    public String getArea() {
        return area;
    }

    public String getDate() {
        return date;
    }

    public String getSex() {
        return sex;
    }

    public String getNewIDCard() {
        return newIDCard;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    /**
     * 输出身份证信息
     * 
     * @param IDCard 身份证号码
     */

    public static String toString(String IDCard) {
        String message;
        IDCard idcard = new IDCard();
        if (idcard.IDCardValidate(IDCard))
        {
            message = String.format("%s性，%s出生，%s省人士", 
                    idcard.getSex(), idcard.getDate(), idcard.getArea());
            if (IDCard.length() == 15)
            {
                message += "，新身份证号是" + idcard.getNewIDCard();
            }
        }
        else
        {
            message = "身份证号无效，" + idcard.getErrorInfo();
        }

        return message;
    }
}