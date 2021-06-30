package com.assessmenttest.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

public class MainApp extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {


//    @Inject
//    DispatchingAndroidInjector<Activity> activityDispatchingAndroidInjector;

    private Activity mCurrentActivity;
    private static MainApp mMainApp;
//    private BasicComponent mBasicComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (mMainApp == null) {
            mMainApp = this;
        }
    }

    public static MainApp getApplication() {
        return mMainApp;
    }

//    public BasicComponent getBasicComponent() {
//        return mBasicComponent;
//    }

    public static Context getContext() {
        return mMainApp.getCurrentActivity() == null ? mMainApp.getApplicationContext() : mMainApp.getCurrentActivity();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mCurrentActivity = activity;
    }


    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }


    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    /**
     * Getter method for current activity
     *
     * @return Current Activity instance
     */
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

}

