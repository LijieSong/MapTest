package com.example.user.maptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    static BDLocation lastLocation = null;
    public static MainActivity instance = null;
    private BaiduSDKReceiver mBaiduReceiver;
    private String lat,log;
    private MyGpsHardware hardware;
    public class BaiduSDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            String st1 = "网络错误";
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(instance, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置", Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(instance, st1, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        //initialize SDK with context, should call this before setContentView
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initData();
    }

    public void initData() {
        showMapWithLocationClient();
        hardware = new MyGpsHardware();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mBaiduReceiver = new BaiduSDKReceiver();
        registerReceiver(mBaiduReceiver, iFilter);
    }

    private void showMapWithLocationClient() {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// open gps
        // option.setCoorType("bd09ll");
        // Johnson change to use gcj02 coordination. chinese national standard
        // so need to conver to bd09 everytime when draw on baidu map
        option.setCoorType("gcj02");
        option.setScanSpan(30000);
        option.setAddrType("all");
        mLocClient.setLocOption(option);
    }

    @Override
    protected void onPause() {
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onPause();
        lastLocation = null;
    }

    @Override
    protected void onResume() {
        if (mLocClient != null) {
            mLocClient.start();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        unregisterReceiver(mBaiduReceiver);
        hardware.close();
        super.onDestroy();
    }
    public void onClick(View view){
        switch (view.getId()){
           case R.id.btn1:
               if (mLocClient != null) {
                   mLocClient.start();
               }
               hardware.open(this, new MyGpsListener() {
                   @Override
                   public void onLocationChanged(MyLocation location) {
                       lat = String.valueOf(location.lat);
                       log = String.valueOf(location.lon);
                       Log.d(TAG, "latitude:" + lat + "-----longitude:" + log);
                       Toast.makeText(instance, "地址:" + location.toString() + "\n" + "经度:" + lat + "\n" + "纬度:" + log, Toast.LENGTH_SHORT).show();
                   }
               });
            break;
            case R.id.btn2:
                showPicturePup(view);
            break;

        }
    }
    /**
     * format new location to string and show on screen
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "On location change received:" + location);
            if (location == null) {
                Toast.makeText(instance, "无法获取到您的位置", Toast.LENGTH_SHORT).show();
                return;
            }
            if (lastLocation != null) {
                if (lastLocation.getLatitude() == location.getLatitude() && lastLocation.getLongitude() == location.getLongitude()) {
                    Log.d(TAG, "same location, skip refresh");
                    // mMapView.refresh(); //need this refresh?
                    return;
                }
            }
            lastLocation = location;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            lat = String.valueOf(latitude);
            log = String.valueOf(longitude);
            Log.d(TAG,location.toString());
            //附近的人及雷达加好友界面及基本功能实现,  网络请求数据未实现 ,其中雷达加好友使用模拟数据展示,附近的人未展示
            Toast.makeText(instance, "地址:" + location.getAddrStr() + "\n" + "经度:" + latitude + "\n" + "纬度:" + longitude, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "addr:" + location.getAddrStr());
            //TODO 获取到经纬度后 请求服务器  查询附近的人 由于没有借口 网络未实现 数据未实现
            Log.d(TAG, "latitude:" + latitude + "-----longitude:" + longitude);
        }
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }

    private void showPicturePup(View view) {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.layout_popupwindown_item, null);
        Button btn_map_baidu = (Button) layout.findViewById(R.id.btn_map_baidu);
        Button btn_map_gaode = (Button) layout.findViewById(R.id.btn_map_gaode);
        Button btn_map_google = (Button) layout.findViewById(R.id.btn_map_google);
        Button btn_map_tencent = (Button) layout.findViewById(R.id.btn_map_tencent);
        Button btn_cancle = (Button) layout.findViewById(R.id.btn_cancle);
            //判断地图是否存在
//        if (!MapUtils.isAvilible(MainActivity.this, "com.baidu.BaiduMap")){
//            btn_map_baidu.setVisibility(View.GONE);
//        }
//        if (!MapUtils.isAvilible(MainActivity.this,"com.autonavi.minimap")){
//            btn_map_gaode.setVisibility(View.GONE);
//        }
//        if (!MapUtils.isAvilible(MainActivity.this,"com.google.android.apps.maps")){
//            btn_map_google.setVisibility(View.GONE);
//        }
//        if (!MapUtils.isAvilible(MainActivity.this,"com.tencent.map")){
//            btn_map_tencent.setVisibility(View.GONE);
//        }
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        //设置键盘不遮盖
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, -location[1]);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupWindow.dismiss();
            }
        });
        btn_map_baidu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                MapUtils.openBaiduMap(MainActivity.this,"39.91516","116.403875");
            }
        });
        btn_map_gaode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                MapUtils.openGDMap(MainActivity.this,"39.91516","116.403875");
            }
        });
        btn_map_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                MapUtils.openGoogleMap(MainActivity.this,"39.91516","116.403875");
            }
        });
        btn_map_tencent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                MapUtils.openTencentMap(MainActivity.this,"绿地赢海国际大厦",lat,log,"39.91516","116.403875","天安门城楼");
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
}
