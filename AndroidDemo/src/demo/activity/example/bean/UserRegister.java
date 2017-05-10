package demo.activity.example.bean;

import engine.android.util.Util;

public class UserRegister {
    
    public String username;                 //用户名
    public String password;                 //密码
    
    public String mobileNumber;             //手机号码
    public String name;                     //真实姓名
    public int sex;                         //性别[0:男,1:女]
    public String email;                    //电子邮箱
    
    @Override
    public String toString() {
        return Util.toString(this);
    }
}