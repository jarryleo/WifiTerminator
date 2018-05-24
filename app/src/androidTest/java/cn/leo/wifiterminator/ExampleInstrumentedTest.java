package cn.leo.wifiterminator;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import cn.leo.wifiterminator.bean.WifiBean;
import cn.leo.wifiterminator.business.WifiProvider;
import cn.leo.wifiterminator.db.WifiDao;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cn.leo.wifiterminator", appContext.getPackageName());
    }

    @Test
    public void connectWifi() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        WifiDao dao = WifiDao.getDao(appContext);
        WifiBean bean = new WifiBean();
        bean.SSID = "aaa";
        bean.BSSID = "bbb";
        bean.password = "ccc";
//        boolean b = dao.addWifi(bean);
        //boolean b = dao.updateWifi(bean);
//        assertEquals(b,true);
        //long lastUpdate = dao.getLastUpdate(bean);
//        String psw = dao.getPsw(bean);
        dao.successPlus(bean);
        int count = dao.getSuccessCount(bean);
        assertEquals(count, 1);
    }
}
