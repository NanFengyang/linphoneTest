package com.nanfeng.pet.yytcallphonedemoapplication.utils;

import android.widget.Toast;
import com.nanfeng.pet.yytcallphonedemoapplication.MyApplition;

/**
 * Created by yangyoutao on 2016/7/27.
 */
public class ToastUtils {

    private ToastUtils()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isShow = true;

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort( CharSequence message)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShort( int message)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong( CharSequence message)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLong(int message)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show( CharSequence message, int duration)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param message
     * @param duration
     */
    public static void show(int message, int duration)
    {
        if (isShow)
            Toast.makeText(MyApplition.getInstance(), message, duration).show();
    }
}
