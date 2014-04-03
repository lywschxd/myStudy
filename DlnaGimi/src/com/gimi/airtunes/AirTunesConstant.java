package com.gimi.airtunes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * 参考 AirTunes 协议: http://nto.github.io/AirPlay.html#servicediscovery-airtunesservice
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirTunesConstant {
	public static String LOG_INFO = AirTunesConstant.class.getName();
	public static final String type = "_raop._tcp.local.";
	public static int port = 49152;
	private static final Map<String, String> txtHead = new HashMap<String, String>();
	
	static {
		/*  定义头信息 */
		txtHead.put("txtvers", "1");    
		txtHead.put("ch",      "2");
		txtHead.put("cn", 	   "0,1,2,3");
		txtHead.put("da",      "true");
		txtHead.put("et", 	   "0,3,5");
		txtHead.put("md",      "0,1,2");
		txtHead.put("pw", 	   "false");
		txtHead.put("sv", 	   "false");
		txtHead.put("sr", 	   "44100");
		txtHead.put("ss", 	   "16"); 		  
		txtHead.put("tp", 	   "UDP"); 		  
		txtHead.put("vn", 	   "65537");	  
		txtHead.put("vs", 	   "130.14");	  
		txtHead.put("am", 	   "AppleTV2,1");  
		txtHead.put("sf",      "0x4");
		Log.w(LOG_INFO + ">>>" + "", "初始化头信息完成<<<");
	}
	
	/**
	 * 将头信息返回.
	 * @return
	 */
	public static Map<String, String> getTxtHead() {
        return Collections.unmodifiableMap(txtHead);
    }
	
}
