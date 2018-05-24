package cn.leo.wifiterminator.business;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.List;

import cn.leo.wifiterminator.bean.WifiBean;
import cn.leo.wifiterminator.utils.WifiUtils;

/**
 * Created by JarryLeo on 2017/2/17.
 */

public class WifiProvider {
    private static final String TAG = "WifiProvider";

    public static List<WifiBean> wifiList(Context context) {
        List<WifiBean> list = new ArrayList<>();
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //mWm.startScan();
        List<ScanResult> results = mWm.getScanResults();
        for (ScanResult sr :
                results) {

            WifiBean bean = new WifiBean();
            bean.SSID = sr.SSID;
            bean.BSSID = sr.BSSID;
            bean.level = sr.level;


            String capabilities = sr.capabilities;

            if (!TextUtils.isEmpty(capabilities)) {

                if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                    bean.type = 3;
                } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                    bean.type = 2;
                } else {
                    //无密码类型
                    bean.type = 1;

                }
            }

            list.add(bean);
        }
        return list;
    }

    /**
     * 获取wifi开关状态
     *
     * @param context
     * @return
     */
    public static boolean wifiOpenState(Context context) {
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return mWm.isWifiEnabled();
    }

    /**
     * 开启wifi
     *
     * @param context
     */
    public static void openWifi(Context context) {
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWm.setWifiEnabled(true);
    }

    /**
     * 连接wifi
     *
     * @param context
     * @param bean
     */
    public static void connectWifi(Context context, WifiBean bean) {
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wcg = WifiUtils.CreateWifiInfo(context, bean.SSID, bean.password, bean.type);
        int wcgID = mWm.addNetwork(wcg);
        boolean b = mWm.enableNetwork(wcgID, true);
    }

    /**
     * 获取当前wifi BSSID
     *
     * @param context
     * @return
     */
    public static String getConnectBSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        //获取当前wifi BSSID
        return wifiInfo.getBSSID();
    }

    /**
     * 联网查询wifi密码
     *
     * @param ssid
     * @param bssid
     * @return
     */
    public static String getWifiPassWord(String ssid, String bssid) {
        String password = null;
        bssid = bssid.replace(":", "").toUpperCase();
        try {
            String url = "http://www.wifi4.cn/wifi/" + ssid + "/" + bssid + "/";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = null;
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                if (!result.contains("未查询到密码")) { //查询到密码
                    int start = result.indexOf("当前密码记录");
                    int end = result.lastIndexOf("当前密码记录");
                    password = result.substring(start + 8, end - 4);
                    //去除多余标签
                    password = password.replace("<font>", "");
                    password = password.replace("</font>", "");
                }
            }
        } catch (Exception e) {

        }
        return password;
    }
}
