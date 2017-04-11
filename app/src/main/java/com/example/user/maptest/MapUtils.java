package com.example.user.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：MapTest
 * 类描述：MapUtils 描述:
 * 创建人：songlijie
 * 创建时间：2017/4/10 10:32
 * 邮箱:814326663@qq.com
 */
public class MapUtils {

    /**
     * 打开高德地图并导航
     * @param context 上下文的对象
     * @param lat  到达的经度
     * @param lng  到达的维度
     */
    public static void openGDMap(Context context, String lat, String lng) {
        Intent intent;
        if (isAvilible(context, "com.autonavi.minimap")) {
            try {
                String appName = context.getString(R.string.app_name);
                intent = Intent.getIntent("androidamap://navi?sourceApplication=" + appName + "&poiname=我的目的地&lat=" + lat + "&lon=" + lng + "&dev=0");
                context.startActivity(intent);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "您尚未安装高德地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
            intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    /**
     * 打开google地图并导航
     *
     * @param context 上下文对象
     * @param lat     目的地经度
     * @param lng     目的地维度
     */
    public static void openGoogleMap(Context context, String lat, String lng) {
        if (isAvilible(context, "com.google.android.apps.maps")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng + ", + Sydney +Australia");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, "您尚未安装谷歌地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    /**
     * 打开百度地图并导航
     * @param context 上下文对象
     * @param lat  目的地经度
     * @param lng  目的地维度
     */
    public static void openBaiduMap(Context context, String lat, String lng) {
        Intent intent;
        if (isAvilible(context, "com.baidu.BaiduMap")) {//传入指定应用包名
            try {
                String appName = context.getString(R.string.app_name);
                intent = Intent.getIntent("intent://map/direction?" +
                        //"origin=latlng:"+"34.264642646862,108.95108518068&" +   //起点  此处不传值默认选择当前位置
                        "destination=latlng:" + lat + "," + lng + "|name:我的目的地" +        //终点
                        "&mode=driving&" +          //导航路线方式
                        "region=北京" +           //
                        "&src=" + appName + "#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
                context.startActivity(intent); //启动调用
            } catch (URISyntaxException e) {
                Log.e("intent", e.getMessage());
            }
        } else {//未安装

            //market为路径，id为包名
            //显示手机上所有的market商店
            Toast.makeText(context, "您尚未安装百度地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.baidu.BaiduMap");
            intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    /**
     * 打开腾讯地图并导航
     * @param context 上下文的对象
     * @param fromlat  起点经度
     * @param fromlng  起点维度
     * @param toAddress 终点地址
     */
    public static void openTencentMap(Context context, String fromAddress,String fromlat, String fromlng,String tolat,String tolng, String toAddress) {
        Intent intent;
        if (isAvilible(context, "com.tencent.map")) {//传入指定应用包名
            try {
                intent = new Intent();
                double[] fromDoubles = MapTranslateUtils.map_bd2hx(Double.valueOf(fromlat), Double.valueOf(fromlng));
                double[] toDoubles = MapTranslateUtils.map_bd2hx(Double.valueOf(tolat), Double.valueOf(tolng));
                String appName = context.getString(R.string.app_name);
                String url ="qqmap://map/routeplan?type=drive&from="+fromAddress+"&fromcoord="
                        + fromDoubles[0] + "," + fromDoubles[1] +"&to="+toAddress+"&" +
                        "tocoord="+toDoubles[0]+","+toDoubles[1]+"&policy=0&referer=" + appName;
                Uri uri = Uri.parse(url);
                intent.setData(uri);
                context.startActivity(intent); //启动调用
            } catch (Exception e) {
                Log.e("intent", e.getMessage());
            }
        } else {//未安装
            //market为路径，id为包名
            //显示手机上所有的market商店
            Toast.makeText(context, "您尚未安装腾讯地图", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.tencent.map");
            intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        //获取packagemanager
        final PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        //用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }
}
