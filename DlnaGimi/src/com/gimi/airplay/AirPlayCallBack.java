package com.gimi.airplay;

import java.io.File;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * AriPlay 回调函数
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayCallBack {
	public static final String VIDEO_STATE_PAUSE  = "0.0"; // 暂停.
	public static final String VIDEO_STATE_RPLAY  = "1.0"; // 继续播放.
	public static final String VIDEO_STATE_STAING    = "STARTING_PLAY"; // 正在播放.
	public static final String VIDEO_STATE_PAUSEING  = "PAUSEING_PAUSE"; // 暂停还是继续播放.
	public static final String VIDEO_STATE_SEEK_POS  = "SEEK_POSITION"; // 快进和快退，跳转.
	
	/* 图片下载完成 */
	public void downImageOver(File file, HttpRequest request) {}
	/* 停止图片会话 */
	public void stopPhotos(){}
	
	/* 视频播放 */
	public void videoPlay(String url) {}
	/* 视频暂停 */
	public void videoPause(String state) {}
	/* 视频跳转 */
	public void videoSeek(double pos) {}
	/* 获取视频当前的播放时间 */
	public double getVideoPostion() { return 0.0; } // 需要 * 0.001，转换成以秒为单位的数量.
	/* 获取视频的总时间 */
	public double getVideoDuration() { return 0.0; }
	
}
