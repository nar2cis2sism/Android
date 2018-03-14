package com.project.network;

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
    
//    /** 用户注册 **/
//    String REGISTER = "register";
//
    /** 获取个人信息 **/
    String GET_USER_INFO = "get_user_info";
//
//    /** 修改个人信息 **/
//    String EDIT_USER_INFO = "edit_user_info";
//
    /** 查询好友列表 **/
    String QUERY_FRIEND_LIST = "query_friend_list";
}