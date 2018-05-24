package cn.leo.wifiterminator.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.leo.wifiterminator.R;
import cn.leo.wifiterminator.bean.WifiBean;
import cn.leo.wifiterminator.business.WifiProvider;
import cn.leo.wifiterminator.db.WifiDao;
import cn.leo.wifiterminator.utils.WifiUtils;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ListView mLvList;
    private Button mBtnScan;
    private ProgressBar mPbProgress;
    private List<WifiBean> mData;
    private WifiAdapter mAdapter;
    private WifiManager mWm;
    private WifiReceiver mReceiver;
    private int[] mLevelIcon = new int[]{
            R.drawable.settings_locked_signal_level_0,
            R.drawable.settings_locked_signal_level_1,
            R.drawable.settings_locked_signal_level_2,
            R.drawable.settings_locked_signal_level_3,
            R.drawable.connect_signal_level_0,
            R.drawable.connect_signal_level_1,
            R.drawable.connect_signal_level_2,
            R.drawable.connect_signal_level_3};
    private ExecutorService mPool = Executors.newFixedThreadPool(10);
    private String mBSSID;
    private boolean isConnected = true;
    private WifiDao mDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initview();
        initData();
        initEvent();
    }


    private void initview() {
        mLvList = (ListView) findViewById(R.id.wifi_lv_list);
        mBtnScan = (Button) findViewById(R.id.wifi_btn_scan);
        mPbProgress = (ProgressBar) findViewById(R.id.wifi_pb_progress);

        //注册接收信息的广播
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void initData() {
        mDao = WifiDao.getDao(this);
        mWm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mData = new ArrayList<>();
        mAdapter = new WifiAdapter();
        mLvList.setAdapter(mAdapter);
        mBtnScan.setEnabled(false);

        //如果wifi未开启就开启,如果开启就加载数据
        if (!WifiProvider.wifiOpenState(this)) {
            WifiProvider.openWifi(this);
        } else {
            mWm.startScan();//开始扫描wifi;
        }
    }

    private void refreshList() {
        List<WifiBean> wifiList = WifiProvider.wifiList(MainActivity.this);
        mData.clear();
        mData.addAll(wifiList);
        searchPsw();//查询密码
        Collections.sort(mData);//信号排序
        mAdapter.notifyDataSetChanged();
        mPbProgress.setVisibility(View.GONE);
        mBtnScan.setEnabled(true);
    }

    //多线程查询密码
    private void searchPsw() {
        mBSSID = WifiProvider.getConnectBSSID(this);
        for (WifiBean bean : mData) {
            if (bean.type == 1) {
                //跳过无密码的
                continue;
            }
            String psw = mDao.getPsw(bean); //从数据获取密码
            long lastUpdate = mDao.getLastUpdate(bean); //获取数据库密码更新时间
            long l = System.currentTimeMillis() - lastUpdate; //距离现在事件间隔
            bean.password = psw;
            //本地数据库无记录或者距离上次更新密码时间大于1天则联网查询密码
            if (l > 86400000 || lastUpdate == 0) {
                //更新密码
                new MyTask().executeOnExecutor(mPool, bean);
            }
        }
    }

    private class MyTask extends AsyncTask<WifiBean, Void, WifiBean> {
        @Override
        protected WifiBean doInBackground(WifiBean... params) {
            WifiBean bean = params[0];
            //从网络获取wifi密码
            String psw = WifiProvider.getWifiPassWord(bean.SSID, bean.BSSID);
            //获取到密码
            bean.password = psw;
            //记录到数据库
            if (!mDao.updateWifi(bean)) {
                //如果数据库不存在就添加
                mDao.addWifi(bean);
            }
            return bean;
        }

        @Override
        protected void onPostExecute(WifiBean bean) {
            Collections.sort(mData);
            mAdapter.notifyDataSetChanged();//密码排序
        }
    }

    private void initEvent() {
        mLvList.setOnItemClickListener(this);
        mBtnScan.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final WifiBean bean = mData.get(position);

        if (bean.BSSID.equals(mBSSID)) {
            WifiInfo wifiInfo = mWm.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("连接信息:");
            builder.setMessage(WifiUtils.getInfoString(mWm));
            builder.setPositiveButton("确定", null);
            builder.show();
            return;
        }


        if (bean.password != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            if (TextUtils.isEmpty(bean.password)) {
                builder.setTitle(bean.SSID + " 警告:");
                builder.setMessage("此wifi热点无密码,请谨慎连接!");
            } else {
                builder.setTitle(bean.SSID + " 的密码:");
                builder.setMessage(bean.password);
            }
            builder.setPositiveButton("一键连接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WifiProvider.connectWifi(MainActivity.this, bean);
                }
            });
            builder.setNegativeButton("输入密码", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    inputPassword(bean);
                }
            });
            builder.show();
        } else {
            inputPassword(bean);
        }
    }

    private void inputPassword(final WifiBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View view1 = View.inflate(MainActivity.this, R.layout.view_password, null);
        builder.setView(view1);
        builder.setTitle(bean.SSID);
        builder.setMessage("请输入WIFI密码:");
        builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText evPsw = (EditText) view1.findViewById(R.id.view_password);
                String psw = evPsw.getText().toString().trim();
                if (TextUtils.isEmpty(psw)) {
                    Toast.makeText(MainActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "正在尝试连接,请稍候...", Toast.LENGTH_SHORT).show();
                    bean.password = psw;
                    WifiProvider.connectWifi(MainActivity.this, bean);
                    //bean.password = null; //清空是为了不显示小钥匙
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public void onClick(View v) {
        mBtnScan.setEnabled(false);
        mWm.startScan(); //重新扫描
        mPbProgress.setVisibility(View.VISIBLE);
    }

    private class WifiAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mData != null) {
                return mData.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mData != null) {
                return mData.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_wifi, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.level = (ImageView) convertView.findViewById(R.id.item_iv_level);
                holder.ssid = (TextView) convertView.findViewById(R.id.item_tv_ssid);
                holder.bssid = (TextView) convertView.findViewById(R.id.item_tv_bssid);
                holder.key = (ImageView) convertView.findViewById(R.id.item_iv_key);
                holder.psw = (TextView) convertView.findViewById(R.id.item_tv_password);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //数据设置
            WifiBean bean = mData.get(position);
            holder.ssid.setText(bean.SSID);
            holder.bssid.setText(bean.BSSID);


            //密码图标和内容
            if (TextUtils.isEmpty(bean.password) && bean.type > 1) {
                holder.key.setVisibility(View.GONE);
                holder.psw.setVisibility(View.GONE);
            } else {
                holder.key.setVisibility(View.VISIBLE);
                holder.psw.setVisibility(View.VISIBLE);
                holder.psw.setText(bean.password);
            }
            //显示已连接的图标
            if (bean.BSSID.equals(mBSSID)) {
                holder.key.setImageResource(R.drawable.wifi_status_connected);
                holder.key.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(bean.password)) {
                    //记录到数据库
                    if (!mDao.updateWifi(bean)) {
                        //如果数据库不存在就添加
                        mDao.addWifi(bean);
                    }
                    //更新成功次数
                    mDao.successPlus(bean);
                }
            } else {
                //网络类型
                if (bean.type > 1) {
                    holder.level.setImageResource(
                            mLevelIcon[mWm.calculateSignalLevel(bean.level, 4)]);
                    holder.key.setImageResource(R.drawable.wifi_status_keyed);
                } else {
                    holder.level.setImageResource(
                            mLevelIcon[mWm.calculateSignalLevel(bean.level, 4) + 4]);
                    bean.password = "";
                    holder.key.setImageResource(R.drawable.safecheck_unusual);
                }
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView level;
        TextView ssid;
        TextView bssid;
        ImageView key;
        TextView psw;
    }

    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                refreshList();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    Toast.makeText(context, "wifi已关闭", Toast.LENGTH_SHORT).show();
                    mData.clear();
                    mAdapter.notifyDataSetChanged();
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Toast.makeText(context, "wifi已开启", Toast.LENGTH_SHORT).show();
                    mWm.startScan(); //wifi开启扫描
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                System.out.println("网络状态改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    //获取当前wifi名称
                    isConnected = true;
                    mBSSID = wifiInfo.getBSSID();
                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(context, "已连接到网络 " + wifiInfo.getSSID(), Toast.LENGTH_SHORT).show();
                } else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if (isConnected) {
                        Toast.makeText(context, "wifi未连接", Toast.LENGTH_SHORT).show();
                        isConnected = false;
                    }
                    mBSSID = null;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
