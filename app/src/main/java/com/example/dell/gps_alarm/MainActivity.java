package com.example.dell.gps_alarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import static com.example.dell.gps_alarm.Alarm.isPlaying;

public class MainActivity extends AppCompatActivity {
    public MapView mMapView = null;
    public BaiduMap baiduMap = null;

    //定位相关声明
    public LocationClient locationClient = null;
    //自定义图标
    BitmapDescriptor mCurrentMarker = null;
    boolean isFirstLoc = true;//是否首次定位
    private double longitude; //得到经度
    private double latitude; //得到纬度
    private double targetLongitude;//记录目标点经度
    private double targetLatitude;//记录目标点纬度
    private Button setTarget;//设置目标点的按钮
    private Button removeTarget;//删除目标点的按钮
    private boolean exist = false;//判断是否已经存在目标点

    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            //map View销毁后不再处理新接收的位置
            if (location == null | mMapView == null)
                return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);//设置定位数据

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);//设置地图中心点、缩放级别
                // MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll);
                baiduMap.animateMapStatus(u);
            }
            if (exist) {
                double s = Alarm.calculateDistance(latitude, targetLatitude,
                        longitude, targetLongitude);
                Toast.makeText(getApplicationContext(), s + "",
                        Toast.LENGTH_SHORT).show();
                if (s <= 100) {
//                    locationClient.stop();
                    setTarget.setText("关闭提示");
                    Alarm.ring(MainActivity.this);
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) this.findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        setTarget = (Button) findViewById(R.id.button);
        setTarget.setOnClickListener(new setTargetClickListener());
        removeTarget = (Button) findViewById(R.id.button2);
        removeTarget.setOnClickListener(new removeTargetClickListener());

        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(myListener);
        this.setLocationOption();
        locationClient.start();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);//设置为卫星地图
        baiduMap.setTrafficEnabled(true);//开启交通图

    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void setLocationOption(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);

        locationClient.setLocOption(option);
    }

    class setTargetClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String s = setTarget.getText().toString();
            if (s.equals("关闭提示")) {
                Alarm.closeRing();
                baiduMap.clear();
                isPlaying = false;
                exist = false;
                setTarget.setText("设置目标点");
                return;
            }
            baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

                public void onMapClick(LatLng point) {
                    if (!exist) {
                        //构建Marker图标
                        BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.loc);
                        //构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option = new MarkerOptions()
                                .position(point)
                                .icon(bitmap);
                        //在地图上添加Marker，并显示
                        baiduMap.addOverlay(option);

                        setTarget.setText("闹钟已经启动");
                        exist = true;
                        targetLongitude = point.longitude;
                        targetLatitude = point.latitude;
                        double s = Alarm.calculateDistance(latitude, targetLatitude,
                                longitude, targetLongitude);
                        Toast.makeText(getApplicationContext(), s + "",
                                Toast.LENGTH_SHORT).show();

                        if (s <= 100) {
                            setTarget.setText("关闭提示");
                            setTarget.setClickable(true);
                            Alarm.ring(MainActivity.this);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "已存在设置完成的目标点",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                public boolean onMapPoiClick(MapPoi poi) {
                    //在此处理底图标注点击事件
                    return false;
                }
            });
        }
//
    }

    class removeTargetClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Alarm.closeRing();
            baiduMap.clear();
            exist = false;
            setTarget.setText("设置目标点");
            isPlaying = false;
        }
    }
}
