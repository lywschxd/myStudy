package com.gimi.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * @描述: Utils 类 主要用于一些常用函数的使用.(有一些代码摘自 DroidAirPlay代码)
 * 
 * @author 	   hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class Utils {
	public static int features = 118;
	public static String systemPathDir = null;
	public static String servName = ""; // 服务名.
	public static final String LOG_INFO = Utils.class.getName();
	/* 用于 MAC 地址获取的保存 */
	public static final byte[] HardwareAddressBytes = getHardwareAddress();
	public static final String HardwareAddressString = toHexString(HardwareAddressBytes);
	public static final String HardwareAddressString2 = toHexString2(HardwareAddressBytes);
	
	public static Channel     ch;
	public static String X_APPLE_SESSION_ID;
	/**
	 * 获取MAC地址.
	 */
	@SuppressLint("NewApi")
	private static byte[] getHardwareAddress() {
		try {
			for (final NetworkInterface iface : Collections
					.list(NetworkInterface.getNetworkInterfaces())) {

				if (iface.isLoopback() || iface.isPointToPoint()) 
					continue;
				
				try {

					final byte[] ifaceMacAddress = iface.getHardwareAddress();
					
					if ((ifaceMacAddress != null)
							&& (ifaceMacAddress.length == 6)
							&& !isBlockedHardwareAddress(ifaceMacAddress)) {
						Log.w(LOG_INFO,
								"Hardware address is "
										+ toHexString(ifaceMacAddress) + " ("
										+ iface.getDisplayName() + ")");
						return Arrays.copyOfRange(ifaceMacAddress, 0, 6);
					}

				} catch (final Throwable e) {
					Log.w(LOG_INFO, "" + e.getMessage());
				}
			}
		} catch (final Throwable e) {
			Log.w(LOG_INFO, "" + e.getMessage());
		}

		try {
			final byte[] hostAddress = Arrays.copyOfRange(InetAddress
					.getLocalHost().getAddress(), 0, 6);
			return hostAddress;
		} catch (final Throwable e) {
			/* ... */
		}

		return new byte[] { (byte) 0x00, (byte) 0xDE, (byte) 0xAD, (byte) 0xBE,
				(byte) 0xEF, (byte) 0x00 };
	}

	public static boolean isBlockedHardwareAddress(final byte[] addr) {
		
		if ((addr[0] & 0x02) != 0) {
			/* Locally administered */
			return true;
		} else if ((addr[0] == 0x00) && (addr[1] == 0x50) && (addr[2] == 0x56)) {
			/* VMware */
			return true;
		} else if ((addr[0] == 0x00) && (addr[1] == 0x1C) && (addr[2] == 0x42)) {
			/* Parallels */
			return true;
		} else if ((addr[0] == 0x00) && (addr[1] == 0x25)
				&& (addr[2] == (byte) 0xAE)) {
			/* Microsoft */
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * byte转换为字符串.
	 */
	private static String toHexString(final byte[] bytes) {
		final StringBuilder s = new StringBuilder();
		
		for (final byte b : bytes) {
			final String h = Integer.toHexString(0x100 | b);
			s.append(h.substring(h.length() - 2, h.length()).toUpperCase());
		}
		
		return s.toString();
	}

	/**
	 * 获取本地地址.
	 */
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName().split("\\.")[0];
		} catch (final Throwable e) {
			return "gimi";
		}
	}
	
	/**
	 * 转换时间为GMT时间.
	 */
	public static String dateToGMT() {
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
		return dateFormat.format(d);
	}
	
	
	/*
	 * 创建图片缓冲目录.
	 */
	public static String createAirPlayPhotosCache(String cacheDir) {
		
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File sdPath = Environment.getExternalStorageDirectory();
			String cachePath = sdPath.getPath() + cacheDir; // "/AirPlayCache";
			File dirFile = new File(cachePath);
			if (!dirFile.exists()) {
				Log.w("CreateAP-PATH:>>>>", "<<<<创建Airplay图片缓冲目录!!");
				dirFile.mkdir();
			}
			return dirFile.getPath();
		}
		
		Log.w(LOG_INFO + ">>>>>>>>>>", "<<<<<<<<无SD卡，使用安装包路径保存图片:" + Utils.systemPathDir);
		/* 如果失败(无SD卡)!! 侧使用安装的目录保存图片 */
		return Utils.systemPathDir;
	}
	
	public static boolean modifySystemDir(String path) {
		String cmd = "chmod " + path + " " + "777" + " && busybox chmod "
				+ path + " " + "777";
		
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static String createSystemDir(Context context) {
		String path = context.getFilesDir().getParent() + File.separator + "AirPlayCache";
		File localFile = new File(path);
		
		if (!localFile.exists()) { // 判断目录是否存在.
			localFile.mkdirs();
			if (!modifySystemDir(path)) {
				return null;
			}
		} 
		
		Utils.systemPathDir = path; // 保存系统创建的目录.
		
		return path;
	}
	
	/**
	 *  判断是否为正在显示的图片.
	 */
	public static boolean isCurrentShowPhotos(HttpRequest request) {
		
		if (request != null && request.getUri().equals("/photo")) {
			if (request.getHeader("X-Apple-AssetAction") == null
					|| request.getHeader("X-Apple-AssetAction").equals(
							"displayCached"))
				return true;
		}
		
		return false;
	}
	
	private static String toHexString2(final byte[] bytes) {
		String s = "";
		
		for (final byte b : bytes) {
			final String h = Integer.toHexString(0x100 | b);
			s += (h.substring(h.length() - 2, h.length()).toUpperCase()) + ":";
		}
		
		return s.substring(0, s.length() - 1);
	}
	
}
