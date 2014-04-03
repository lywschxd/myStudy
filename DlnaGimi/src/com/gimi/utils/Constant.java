package com.gimi.utils;

import com.gimi.airplay.AirPlayMediaPlayer;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * @描述: Utils 类 主要用于一些常量的使用.
 * 
 * @author 	   hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class Constant {
	
	/* 服务端发送一个停止图片会话 */
	public static final String STOP_PHOTO_SESSION = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\r\n" + 
			"\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n" + 
			"<plist version=\"1.0\">\r\n" + 
			"<dict>\r\n" + 
			"<key>category</key>\r\n" + 
			"<string>photo</string>\r\n" + 
			"<key>sessionID</key>\r\n" + 
			"<integer>1</integer>\r\n" + 
			"<key>state</key>\r\n" + 
			"<string>stopped</string>\r\n" + 
			"</dict>\r\n" + 
			"</plist>\r\n";
	
	/* server-info XML 信息. */
	public static String FETCH_SERVER_INFORMATIONS = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\r\n"
			+ "http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n"
			+ "<plist version=\"1.0\">\r\n"
			+ "<dict>\r\n"
			+ "<key>deviceid</key>\r\n"
			+ "<string>"
			+ Utils.HardwareAddressString2
			+ "</string>\r\n"
			+ "<key>features</key>\r\n"
			+ "<integer>"  + Utils.features++ + "</integer>\r\n"
			+ "<key>model</key>\r\n"
			+ "<string>AppleTV2,1</string>\r\n"
			+ "<key>protovers</key>\r\n"
			+ "<string>1.0</string>\r\n"
			+ "<key>srcvers</key>\r\n"
			+ "<string>101.28</string>\r\n"
			+ "</dict>\r\n" + "</plist>\r\n";
	
	/* 视频回复的 VIDEO EVENT 状态 */
	public static String VIDEO_EVNET_INFO = "" + 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\r\n" +
			"\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n" +
			"<plist version=\"1.0\">\r\n" +
			"<dict>\r\n" +
			"<key>category</key>\r\n" +
			"<string>" + AirPlayMediaPlayer.category + "</string>\r\n" +
			"<key>state</key>\r\n" +
			"<string>" + AirPlayMediaPlayer.state + "</string>\r\n" +
			"</dict>\r\n" +
			"</plist>\r\n";

	/* 视频的 video playback-info 信息确认 */
	public static String PLAYBACK_INFO = "" + 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\"\r\n" +
			"\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n" +
			"<plist version=\"1.0\">\r\n" +
			"<dict>\r\n" +
			"<key>duration</key> <real>" + AirPlayMediaPlayer.duration + "</real>\r\n" +
			"<key>loadedTimeRanges</key>\r\n" +
			"<array>\r\n" +
			"<dict>\r\n" +
			"<key>duration</key> <real>" + AirPlayMediaPlayer.duration + "</real>\r\n" +
			"<key>start</key> <real>" + AirPlayMediaPlayer.start + "</real>\r\n" +
			"</dict>\r\n" +
			"</array>\r\n" +
			"<key>playbackBufferEmpty</key> <true/>\r\n" +
			"<key>playbackBufferFull</key> <false/>\r\n" +
			"<key>playbackLikelyToKeepUp</key> <true/>\r\n" +
			"<key>position</key> <real>" + AirPlayMediaPlayer.position + "</real>\r\n" +
			"<key>rate</key> <real>" + AirPlayMediaPlayer.rate.substring(0) + "</real>\r\n" +
			"<key>readyToPlay</key> <true/>\r\n" +
			"<key>seekableTimeRanges</key>\r\n" +
			"<array>\r\n" +
			"<dict>\r\n" +
			"<key>duration</key>\r\n" +
			"<real>" + AirPlayMediaPlayer.duration + "</real>\r\n" +
			"<key>start</key>\r\n" +
			"<real>" + AirPlayMediaPlayer.start + "</real>\r\n" +
			"</dict>\r\n" +
			"</array>\r\n" +
			"</dict>\r\n" + 
			"</plist>\r\n";

}
