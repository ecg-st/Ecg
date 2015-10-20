package com.nju.ecg.location;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.nju.ecg.service.EcgApp;
import com.nju.ecg.utils.LogUtil;
/**
 * 定位服务
 * @author zhuhf
 * @version [2013-3-21]
 */
public class LocationManager
{
    private static final String TAG = "JzgdLocationManager";
    public LocationClient mLocationClient = null;
    private static LocationManager sInstance;
    private LocationResultListener resultListener;
    private LocationProgressListenr progressListenr;
    
    private LocationManager()
    {
        mLocationClient = new LocationClient(EcgApp.getInstance().getContext());
        mLocationClient.registerLocationListener(new MyBDLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 启用GPS
        option.setPriority(LocationClientOption.GpsFirst);// 设置GPS定位优先
        option.disableCache(true);// 禁用缓存定位
        option.setAddrType("all");// 返回文字描述的地理信息
        mLocationClient.setLocOption(option);
    }
    
    public static LocationManager getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new LocationManager();
        }
        return sInstance;
    }
    
    /**
     * 启动定位服务
     */
    public void startService()
    {
        mLocationClient.start();
    }
    
    /**
     * 关闭定位服务
     */
    public void stopService()
    {
        mLocationClient.stop();
    }
    
    /**
     * 请求地理位置, 一次定位模式
     * @param needProgress 是否显示进度
     * @param resultListener 定位结果监听器
     * @param progressListenr 进度监听器
     */
    public void requestLocation(boolean showProgress, LocationResultListener resultListener, LocationProgressListenr progressListenr)
    {
        this.resultListener = resultListener;
        this.progressListenr = progressListenr;
        if (!mLocationClient.isStarted())
        {
            startService();
        }
        // 打开进度更新
        if (this.progressListenr != null)
        {
            this.progressListenr.onLocationProgress(showProgress);
        }
        if (mLocationClient.isStarted())
        {
            // 发起定位请求
            mLocationClient.requestLocation();
        }
    }
    
    /**
     * 定位结果异步监听器
     * [errorCode说明：
     *  61 ： GPS定位结果
        62 ： 扫描整合定位依据失败。此时定位结果无效。
        63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
        65 ： 定位缓存的结果。
        66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
        67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
        68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
        161： 表示网络定位结果
        162~167： 服务端定位失败。]
     * @author zhuhf
     * @version [2013-3-21]
     */
    public class MyBDLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation location)
        {
            /**
             * 定位SDK start之后立即执行，这种情况下很难定位成功，因为定位SDK刚开始启动还没有获取到定位信息。
             * 这时getlocation一般为null。如果是要获取位置成功，可以在listerner中添加一个判断如果strData为空，则再发起一次定位。
             */
            if (location == null)
            {
                mLocationClient.requestLocation();
                return;
            }
            int errorCode = location.getLocType();
            if (errorCode == 63)// 网络异常，没有成功向服务器发起请求。此时定位结果无效。
            {
                if (resultListener != null)
                {
                    resultListener.locationError(ErrorTypes.ERROR_LOCATION_ERROR);
                }
            }
            else if (errorCode >= 162 && errorCode <= 167)// 服务端定位失败
            {
                if (resultListener != null)
                {
                    resultListener.locationError(ErrorTypes.ERROR_LOCATION_ERROR);
                }
            }
            else// 定位成功
            {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LogUtil.d(TAG, "定位成功 >>> latitude:" + latitude + "  longitude:" + longitude + "  addr:" + location.getAddrStr());
                LocationInfo info = new LocationInfo();
                info.setLatitude(latitude);
                info.setLongitude(longitude);
                info.setAddressInfo(location.getAddrStr());
                if (resultListener != null)
                {
                    resultListener.onLocationResult(info);
                }
            }
            // 关闭定位服务
            stopService();
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation)
        {
        }
    }
    
    /**
     * 定位结果回调
     * @author zhuhf
     * @version [2013-3-21]
     */
    public interface LocationResultListener
    {
        /**
         * 定位成功结果返回
         * @param locationInfo 定位结果
         */
        void onLocationResult(LocationInfo locationInfo);
        
        /**
         * 定位失败
         * @param errorTypes 失败则不为空, 标识失败的具体原因
         */
        void locationError(ErrorTypes errorTypes);
    }
    
    /**
     * 
     * 获取定位信息的进度更新
     * @author zhuhf
     * @version [2013-3-21]
     */
    public interface LocationProgressListenr
    {
        /**
         * 控制弹出框是否显示
         * @param showProgress true为显示，false为不显示
         */
        void onLocationProgress(boolean showProgress);
    }
    
    /*
     * 定义错误类型
     */
    public enum ErrorTypes
    {
        ERROR_UNKNOWN, // 未知错误(用于定义通用提示"操作失败")
        ERROR_LOCATION_ERROR, // 定位失败
    }
}
