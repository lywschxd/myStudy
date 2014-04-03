package com.gimi.service;

import java.io.File;
import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpRequest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.gimi.airplay.AirPlayCallBack;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 
 * @描述: AirPlayServer 主要用于开启 AirPlay 服务，
 * 这个服务主要用于处理图像以及视频,
 * 详情参考(http://nto.github.io/AirPlay.html#introduction).
 * (有一些代码摘自 DroidAirPlay代码)
 * 
 * 注意: <service android:name="com.gimi.service.AirPlayService"/>  XML -> 需要加入这句.
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayService extends Service {
	
	private final String LOG_INFO = AirPlayService.class.getSimpleName();
	
	private RAOPServerRunnable raopServer;
	private AirPlayServerRunnable airPlayServer;
	
	private Thread ariPlayThread = null;
	private Thread raopThread = null;
	
	/**
	 * 进行传过来的参数处理.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(LOG_INFO + ">>>", "<<<" + "onStartCommand");
		
		if (intent != null) {
			String actionStr = intent.getAction();
			/* */
			if (actionStr != null) {
				/* */
				if (actionStr.equals("SERVER_NAME")) { /* 更改服务名 */
					// Utils.servName = "";
				}
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.w(LOG_INFO + ">>>", "<<<" + "onBind");
		return null;
	}
	
	/**
	 * 初始服务.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		AirPlayCallBack cb = new AirPlayCallBack() {
			@Override
			public void downImageOver(File file, HttpRequest request) {
				super.downImageOver(file, request);
			}
		};
		Log.w(LOG_INFO + ">>>", "<<<" + "onCreate");
		airPlayServer = new AirPlayServerRunnable(cb);
		raopServer = new RAOPServerRunnable();
	}

	/**
	 * 开启服务.
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.w(LOG_INFO + ">>>", "<<<" + "onStart");
		ariPlayThread = new Thread(airPlayServer); // .start();  /* 开启 AirPlay 服务 */
		raopThread = new Thread(raopServer); // .start();     /* 开启 Raop 服务        */
		ariPlayThread.start();
		raopThread.start();
	}
	
	/**
	 * 停止服务.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(LOG_INFO + ">>>", "<<<" + "onDestroy");
		try {
			airPlayServer.onShutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
		raopServer.onShutdown();
	}
	
}
