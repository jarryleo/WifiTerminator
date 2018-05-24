package cn.leo.wifiterminator.bean;

import android.text.TextUtils;
import android.view.TextureView;

/**
 * Created by JarryLeo on 2017/2/17.
 */

public class WifiBean implements Comparable<WifiBean> {
    public String SSID;
    public String BSSID;
    public int level;
    public int type;
    public String password;

    @Override
    public int compareTo(WifiBean o) {
        int num;
        int l = TextUtils.isEmpty(password) ? 0 : 1;
        int r = TextUtils.isEmpty(o.password) ? 0 : 1;
        num = r - l;
        num = num == 0 ? o.level - level : num;
        return num == 0 ? SSID.compareTo(o.SSID) : num;
    }
}
