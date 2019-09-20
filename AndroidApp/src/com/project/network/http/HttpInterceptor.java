package com.project.network.http;

import engine.android.framework.network.ConnectionStatus.ConnectionInterceptor;

import com.project.network.action.Actions;

public class HttpInterceptor implements ConnectionInterceptor, Actions {

    @Override
    public boolean intercept(String action, int status, Object param) {
        // TODO Auto-generated method stub
        return false;
    }
}