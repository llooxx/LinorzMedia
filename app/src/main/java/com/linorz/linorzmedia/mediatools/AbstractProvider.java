package com.linorz.linorzmedia.mediatools;

import android.content.Context;

import java.util.List;

public abstract class AbstractProvider {
    //抽象的媒体获得类
    protected Context context;

    public AbstractProvider(Context context) {
        this.context = context;
    }

    public abstract List<?> getList();
}