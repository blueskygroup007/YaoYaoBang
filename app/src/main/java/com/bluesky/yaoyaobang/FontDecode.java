package com.bluesky.yaoyaobang;

import java.io.UnsupportedEncodingException;

/**
 * @author BlueSky
 * @date 23.2.18
 * Description:
 */
public final class FontDecode {
    String ENCODE="GB2312";
    public static byte[] decode(String str){
        byte[] b=null;
        try {
            b=str.getBytes("GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }
}
