package com.thunder.entertainment.common.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.gyf.barlibrary.ImmersionBar;
import com.thunder.entertainment.R;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by beibeizhu on 17/6/12.
 */

public abstract class BaseActivity<P extends BasePresenter> extends RxAppCompatActivity implements BaseView {

    protected P mPresenter;
    protected Unbinder mUnbinder;
    protected Activity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mUnbinder = ButterKnife.bind(this);
        if (mPresenter != null)
            mPresenter.attachView(this);
        mContext = this;

        ImmersionBar.with(this)
                .statusBarColor(R.color.colorPrimary)
                .init();

        initView();
        initEvent();
        initData();
    }

    protected abstract void initView();

    protected abstract int getLayoutId();

    protected abstract void initEvent();

    protected abstract void initData();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null)
            mPresenter.detachView();
        if (mUnbinder == null)
            mUnbinder.unbind();

        //不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        ImmersionBar.with(this).destroy();
    }
}
