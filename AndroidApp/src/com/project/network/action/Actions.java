package com.project.network.action;

/**
 * 网络信令集
 * 
 * @author Daimon
 */
public interface Actions {

    /** 获取导航配置 **/
    String NAVIGATION = "navigation";

    /** 用户登录 **/
    String LOGIN = "login";

    /** 用户注销 **/
    String LOGOUT = "logout";

    /** 获取手机验证码 **/
    String GET_SMS_CODE = "get_sms_code";
    
    /** 用户注册 **/
    String REGISTER = "register";

    /** 获取个人信息 **/
    String GET_USER_INFO = "get_user_info";
    
    /** 修改个人信息 **/
    String EDIT_USER_INFO = "edit_user_info";

    /** 获取好友信息 **/
    String GET_FRIEND_INFO = "get_friend_info";

    /** 查询好友列表 **/
    String QUERY_FRIEND_LIST = "query_friend_list";

    /** 搜索联系人 **/
    String SEARCH_CONTACT = "search_contact";
    
    /** 添加删除好友 **/
    String ADD_FRIEND = "add_friend";

    /******************************* 华丽丽的分割线 *******************************/
    
    /** 实名认证 **/
    String AUTHENTICATION = "authentication";
    
    /** 上传头像 **/
    String AVATAR = "avatar";
}