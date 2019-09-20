package com.project.network.socket;

import engine.android.framework.network.ConnectionStatus.ConnectionInterceptor;

import com.project.network.action.Actions;

public class SocketInterceptor implements ConnectionInterceptor, Actions {

    @Override
    public boolean intercept(String action, int status, Object param) {
        // TODO Auto-generated method stub
        return false;
    }
}