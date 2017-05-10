package demo.activity.example.bean;

import engine.android.dao.annotation.DAOPrimaryKey;
import engine.android.dao.annotation.DAOProperty;
import engine.android.dao.annotation.DAOTable;

/**
 * 账户属性
 * @author yanhao
 * @version 1.0
 */

@DAOTable
public class LoginBean {
	
    @DAOPrimaryKey
	public String account;					//帐号
    @DAOProperty
	public String password;					//密码

    @DAOProperty
	public String rem_pwd;					//记住密码
    @DAOProperty
	public String yinshen;					//隐身登录
    @DAOProperty
	public String zhendong;					//开启振动
    @DAOProperty
	public String qunxiaoxi;				//接收群消息
    @DAOProperty
	public String jingyin;					//静音登录
}