package com.nanfeng.pet.yytcallphonedemoapplication.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nanfeng.pet.yytcallphonedemoapplication.R;

/**
 * 资源泛型：可以为string可，uri类型，File file，Integer resourceId，byte[] model；
 * Created by yangyoutao on 2016/7/27.
 */
public class ImageHelper {

    private static int mPlaceHolderRouseId = R.mipmap.ic_launcher;
    private static int mErrorId = R.mipmap.ic_launcher;
    private static int mDefultRoundDp = 8;//默认圆角角度

    /**
     * 加载url图片
     *
     * @param context
     * @param resource
     * @param imageView
     */
    public static <T> void load(Context context, T resource, ImageView imageView) {
        Glide.with(context)
                .load(resource)
                .error(mErrorId)
                .crossFade()//淡入淡出
                .into(imageView);
    }

    /**
     * 加载网络gif动画
     *
     * @param context
     * @param resource
     * @param imageView
     */
    public static <T> void loadGif(Context context, T resource, ImageView imageView) {
        Glide.with(context)
                .load(resource)
                .asGif() //判断加载的url资源是否为gif格式的资源
                .error(mErrorId)
                .crossFade()
                .into(imageView);
    }

    /**
     * 网络图片 圆形处理
     *
     * @param context
     * @param resource
     * @param imageView
     */
    public static <T> void loadCircle(Context context, T resource, ImageView imageView) {
        Glide.with(context)
                .load(resource)
                .error(mErrorId)
                .crossFade()
                .transform(new GlideCircleTransform(context)).into(imageView);
    }

    /**
     * 网络图片圆角处理，自定义角度
     *
     * @param context
     * @param resource
     * @param imageView
     * @param dp
     */
    public static <T> void loadRound(Context context, T resource, ImageView imageView, int dp) {
        Glide.with(context)
                .load(resource)
                .error(mErrorId)
                .crossFade()
                .transform(new GlideRoundTransform(context, dp)).into(imageView);
    }

    /**
     * 网络图片圆角处理，默认角度
     *
     * @param context
     * @param resource
     * @param imageView
     */
    public static <T> void loadRound(Context context, T resource, ImageView imageView) {
        Glide.with(context)
                .load(resource)
                .error(mErrorId)
                .crossFade()
                .transform(new GlideRoundTransform(context, mDefultRoundDp)).into(imageView);
    }
}
