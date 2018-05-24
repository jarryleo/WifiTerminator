package cn.leo.wifiterminator;

import android.os.Debug;

import org.junit.Test;

import cn.leo.wifiterminator.business.WifiProvider;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        String psw = WifiProvider.getWifiPassWord("TP-LINK_298D", "F483CD34298D");

        assertEquals("PV5843Xotb", psw);
    }
}