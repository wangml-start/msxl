package com.cgmn.msxl;

import com.cgmn.msxl.utils.AESUtil;
import com.cgmn.msxl.utils.MessageUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void cuTest(){
        String ss = AESUtil.encrypt("123456789", MessageUtil.SERCURETY);
        System.out.println(ss);
        System.out.println(AESUtil.decrypt(ss, MessageUtil.SERCURETY));
    }
}