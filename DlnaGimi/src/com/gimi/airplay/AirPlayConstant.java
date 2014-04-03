package com.gimi.airplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.gimi.utils.Utils;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * 参考 AirPlay 协议: http://nto.github.io/AirPlay.html#resources-appleprotocols
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayConstant {
	public static String LOG_INFO = AirPlayConstant.class.getName();
	public static final String type = "_airplay._tcp.local.";
	public static int port = 7000;    // 端口.
	private static final Map<String, String> txtHead = new HashMap<String, String>();
	
	static {
		/*  定义头信息 */
		txtHead.put("deviceid",      Utils.HardwareAddressString);    // MAC地址.
		Log.w(LOG_INFO + ">>>>Mac 地址：", ">>>" + Utils.HardwareAddressString + "<<<<");
		txtHead.put("features",      "0x39f7");
		txtHead.put("model", 	     "AppleTV2,1");
		txtHead.put("srcvers",       "130.14");
		Log.w(LOG_INFO + ">>>" + "", "初始化头信息完成<<<");
	}
	
	/**
	 * 将协议的头信息返回.
	 */
	public static Map<String, String> getTxtHead() {
        return Collections.unmodifiableMap(txtHead);
    }
	
}
