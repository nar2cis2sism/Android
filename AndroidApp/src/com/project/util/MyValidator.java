package com.project.util;

import java.util.regex.Pattern;

public class MyValidator extends engine.android.util.ui.MyValidator {

    /** 密码 */
    public static final Pattern PASSWORD
    = Pattern.compile("^\\S{6,20}$");

    /** 验证码 */
    public static final Pattern PASSCODE
    = Pattern.compile("^\\d{4}$");

    /** 钱 */
    public static final Pattern MONEY
    = Pattern.compile("^\\d+(\\.\\d{0,2})?$");
}