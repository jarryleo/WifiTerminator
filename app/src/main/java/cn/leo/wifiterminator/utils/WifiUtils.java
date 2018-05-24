package cn.leo.wifiterminator.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.util.List;

/**
 * Created by JarryLeo on 2017/2/17.
 */

public class WifiUtils {
    public static WifiConfiguration CreateWifiInfo(Context context, String SSID, String Password, int Type) {
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = IsExsits(context, SSID);
        if (tempConfig != null) {
            mWm.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private static WifiConfiguration IsExsits(Context context, String SSID) {
        WifiManager mWm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs = mWm.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("/" + SSID + " / ")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * int型ip地址转字符串
     *
     * @param ip
     * @return
     */
    public static String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getInfoString(WifiManager wm) {
        WifiInfo wifiInfo = wm.getConnectionInfo();
        DhcpInfo dhcpInfo = wm.getDhcpInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("IP地址：" + ipIntToString(wifiInfo.getIpAddress()) + "\n");
        sb.append("网关：" + ipIntToString(dhcpInfo.gateway) + "\n");
        sb.append("网络ID：" + wifiInfo.getNetworkId() + "\n");
        sb.append("网络名字：" + wifiInfo.getSSID() + "\n");
        sb.append("网络的信号：" + wifiInfo.getRssi() + "\n");
        sb.append("网络连接的速度：" + wifiInfo.getLinkSpeed() + "\n");
        //sb.append("客户端状态的信息：" + wifiInfo.getSupplicantState() + "\n");
        return sb.toString();
    }


}
