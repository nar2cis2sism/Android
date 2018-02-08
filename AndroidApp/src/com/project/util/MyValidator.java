package com.project.util;

import java.util.regex.Pattern;

public class MyValidator extends engine.android.util.ui.MyValidator {

    /** 登录密码 */
    public static final Pattern LOGIN_PASSWORD
    = Pattern.compile("^\\S{6,16}$");
}