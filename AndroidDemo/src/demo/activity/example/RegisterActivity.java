//package demo.activity.example;
//
//import android.os.Bundle;
//import android.os.SystemClock;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.RadioGroup;
//
//import demo.activity.example.bean.UserRegister;
//import demo.android.R;
//import engine.android.core.Forelet;
//import engine.android.core.Forelet.Task.TaskCallback;
//import engine.android.core.Forelet.Task.TaskExecutor;
//import engine.android.core.annotation.InjectView;
//import engine.android.util.MyValidator;
//import engine.android.util.MyValidator.PatternValidation;
//
//import java.util.regex.Pattern;
//
//public class RegisterActivity extends Forelet implements OnClickListener {
//    
//    @InjectView(id=R.id.username)
//    EditText username;
//    @InjectView(id=R.id.password)
//    EditText password;
//    @InjectView(id=R.id.password_confirm)
//    EditText password_confirm;
//    @InjectView(id=R.id.mobile_number)
//    EditText mobile_number;
//    @InjectView(id=R.id.name)
//    EditText name;
//    @InjectView(id=R.id.sex)
//    RadioGroup sex;
//    @InjectView(id=R.id.email)
//    EditText email;
//    @InjectView(id=R.id.register)
//    Button register;
//    @InjectView(id=R.id.back)
//    Button back;
//    
//    UserRegister userRegister = new UserRegister();
//    
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.register_activity);
//        
//        setListener();
//    }
//    
//    private void setListener()
//    {
//        //用户名
//        bindValidation(username, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "请输入用户名")
//                .addValidation(new PatternValidation<EditText>(MyValidator.patternAccount(6, 12)), "用户名长度为6到12字节，允许数字下划线"));
//        bindJavaBean(username, new TextJavaBean<EditText>() {
//
//            @Override
//            public String get() {
//                return userRegister.username;
//            }
//
//            @Override
//            public void set(String s) {
//                userRegister.username = s;
//            }
//        });
//        
//        //密码
//        bindValidation(password, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "请输入密码")
//                .addValidation(new PatternValidation<EditText>(Pattern.compile("^\\S{6,}$")), "密码长度不能小于6"));
//        bindJavaBean(password, new TextJavaBean<EditText>() {
//
//            @Override
//            public String get() {
//                return userRegister.password;
//            }
//
//            @Override
//            public void set(String s) {
//                userRegister.password = s;
//            }
//        });
//        
//        //密码确认
//        bindValidation(password_confirm, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "请再次输入密码")
//                .addValidation(new TextValidation<EditText>() {
//
//                    @Override
//                    public boolean isValid(String s) {
//                        return s.equals(password.getText().toString());
//                    }
//                }, "密码输入不一致，请重新输入"));
//        
//        //手机号码
//        bindValidation(mobile_number, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "请输入手机号码")
//                .addValidation(new PatternValidation<EditText>(MyValidator.MOBILE_NUMBER), "手机号码格式不正确"));
//        bindJavaBean(mobile_number, new TextJavaBean<EditText>() {
//
//            @Override
//            public String get() {
//                return userRegister.mobileNumber;
//            }
//
//            @Override
//            public void set(String s) {
//                userRegister.mobileNumber = s;
//            }
//        });
//        
//        //真实姓名
//        bindValidation(name, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "真实姓名不能为空")
//                .addValidation(new TextValidation<EditText>() {
//
//                    @Override
//                    public boolean isValid(String s) {
//                        for (int i = 0, len = s.length(); i < len; i++)
//                        {
//                            char c = s.charAt(i);
//                            if (MyValidator.validate(String.valueOf(c), MyValidator.CHINESE))
//                            {
//                                return true;
//                            }
//                        }
//                        
//                        return false;
//                    }
//                }, "姓名必须包含汉字"));
//        bindJavaBean(name, new TextJavaBean<EditText>() {
//
//            @Override
//            public String get() {
//                return userRegister.name;
//            }
//
//            @Override
//            public void set(String s) {
//                userRegister.name = s;
//            }
//        });
//        
//        //性别
//        bindJavaBean(sex, new JavaBean<RadioGroup>() {
//
//            @Override
//            public void setValueTo(RadioGroup view) {
//                view.check(userRegister.sex == 0 ? R.id.sex_male : R.id.sex_female);
//            }
//
//            @Override
//            public void getValueFrom(RadioGroup view) {
//                userRegister.sex = view.getCheckedRadioButtonId() == R.id.sex_male ? 0 : 1;
//            }
//        });
//        
//        //电子邮箱
//        bindValidation(email, new MyValidator.Validation<EditText>()
//                .addValidation(new PatternValidation<EditText>(MyValidator.VALID), "请输入邮箱")
//                .addValidation(new PatternValidation<EditText>(MyValidator.EMAIL_ADDRESS), "邮箱格式不正确"));
//        bindJavaBean(email, new TextJavaBean<EditText>() {
//
//            @Override
//            public String get() {
//                return userRegister.email;
//            }
//
//            @Override
//            public void set(String s) {
//                userRegister.email = s;
//            }
//        });
//        
//        //注册
//        register.setOnClickListener(this);
//        
//        //返回
//        back.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//        case R.id.register:
//            //注册
//            if (requestValidation())
//            {
//                fillBeanFromView();
//                
//                showProgress(new ProgressSetting().setMessage("正在注册，请稍后..."));
//                executeTask(new Task(new TaskExecutor() {
//                    
//                    @Override
//                    public Object doExecute() {
//                        SystemClock.sleep(3000);
//                        return null;
//                    }
//                    
//                    @Override
//                    public void cancel() {
//                        // TODO Auto-generated method stub
//                        
//                    }
//                }, new TaskCallback() {
//                    
//                    @Override
//                    public void onFinished(Object result) {
//                        openMessageDialog("注册成功", userRegister.toString(), "确定");
//                        hideProgress();
//                    }
//                }));
//            }
//            
//            break;
//        case R.id.back:
//            //返回
//            onBackPressed();
//            break;
//        }
//    }
//    
//    @Override
//    public boolean onSearchRequested() {
//        userRegister.username = "nar2cis2sism";
//        userRegister.password = "avc84^";
//        userRegister.mobileNumber = "18222776787";
//        userRegister.name = "闫昊";
//        userRegister.sex = 0;
//        userRegister.email = "nar2cis2sism@163.com";
//        fillViewFromBean();
//        password_confirm.setText(userRegister.password);
//        
//        return false;
//    }
//}