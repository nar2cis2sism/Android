package demo.activity.test;

import android.os.Bundle;

import engine.android.util.IDCard;

public class TestOnUtil extends TestOnBase {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String idCard = "420583198608030017";
        log(String.format("身份证号：%s\n个人信息：%s", idCard, IDCard.toString(idCard)));
        
        showContent();
    }
}