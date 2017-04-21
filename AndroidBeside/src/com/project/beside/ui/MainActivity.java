package com.project.beside.ui;

import android.app.Fragment;

import engine.android.framework.ui.extra.SinglePaneActivity;

public class MainActivity extends SinglePaneActivity {
    
    @Override
    protected Fragment onCreateFragment() {
        return new BesideFragment();
    }
}