package com.project.util;

import java.util.regex.Pattern;

public class MyValidator extends engine.android.util.ui.MyValidator {

    /** 密码 */
    public static final Pattern PASSWORD
    = Pattern.compile("^\\S{6,16}$");
}