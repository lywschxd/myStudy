package com.gimi.airplay;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * AriPlay 播放器的的参数保存，
 * 比如 当前的进度，
 * 播放的状态（loading, playing, paused or stopped)等等!!
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayMediaPlayer {
	public static final String STATE_LOADING  = "loading";
	public static final String STATE_PLAYING  = "playing";
	public static final String STATE_PAUSED   = "paused";
	public static final String STATE_STOPPED  = "stopped";
	public static final String RATE_ERROR_STATE = "404.0";
	
	public static String start          = "0.0";
	public static String url            = "";
	public static String category 		= "video";
	public static String state 			= "loading"; // loading, playing, paused or stopped
	
	public static double  position  	= 0.0; // 当前播放进度.
	public static double duration       = 0.0; // 总进度
	
	public static String rate           = "0"; // 暂停 还是 继续播放.
	
}
