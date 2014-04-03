package com.gimi.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

import android.annotation.SuppressLint;
import android.util.Log;

import com.gimi.airplay.AirPlayCallBack;
import com.gimi.airplay.AirPlayConstant;
import com.gimi.airplay.AirPlayPipelineFactory;
import com.gimi.utils.Constant;
import com.gimi.utils.Utils;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * @描述: AirPlayServer 主要用于开启 AirPlay 服务， 这个服务主要用于处理图像以及视频,
 *      详情参考(http://nto.github.io/AirPlay.html#introduction). (有一些代码摘自
 *      DroidAirPlay代码)
 * 
 * @author hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayServerRunnable implements Runnable {
	public static final String LOG_INFO = AirPlayServerRunnable.class.getName();
	static final Logger LOGGER = Logger.getLogger(AirPlayServerRunnable.class
			.getName());
	public static final List<JmDNS> s_jmDNSInstances = new java.util.LinkedList<JmDNS>();

	public static ChannelGroup channelGroup = new DefaultChannelGroup();

	AirPlayCallBack cb = null;

	public AirPlayServerRunnable(AirPlayCallBack cb) {
		this.cb = cb;
	}

	public void run() {
		startService(); // 启动服务.
		startDNSService(); // 启动DNS服务.
	}

	void startService() {
		/* 初始化服务 */
		ExecutorService executorService1 = Executors.newCachedThreadPool();
		ExecutorService executorService2 = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(
				executorService1, executorService2);
		ServerBootstrap serverBootstrap = new ServerBootstrap(channelFactory);
		/* 设置管道 */
		serverBootstrap.setPipelineFactory(new AirPlayPipelineFactory(cb));
		/* 设置属性 */
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive",  true);
		serverBootstrap.setOption("reuseAddress", 	  true);
		/* 监听 */
		Channel ch = serverBootstrap.bind(new InetSocketAddress(
				AirPlayConstant.port));
		Utils.ch = ch; // 保存 channel 句柄.
		channelGroup.add(ch);
	}

	/**
	 * 启动NDS服务，让苹果设备能发现我们的服务.
	 */
	@SuppressLint("NewApi")
	void startDNSService() {
		/* Create mDNS responders. */
//		synchronized (s_jmDNSInstances) {
//			try {
//				for (final NetworkInterface iface : Collections
//						.list(NetworkInterface.getNetworkInterfaces())) {
//
//					if (iface.isLoopback() || iface.isPointToPoint()
//							|| !iface.isUp())
//						continue;
//
//					for (final InetAddress addr : Collections.list(iface
//							.getInetAddresses())) {
//						if (!(addr instanceof Inet4Address)
//								&& !(addr instanceof Inet6Address))
//							continue;

						try {
							/* Create mDNS responder for address */
							final JmDNS jmDNS = JmDNS.create();
							s_jmDNSInstances.add(jmDNS);
							/* 初始化服务名 */
							String name = Utils.servName.length() > 0 ? Utils.servName
									: Utils.HardwareAddressString + "@XGIMI"
											+ "(" + "极米" + ")";
							Utils.servName = name;
							Log.w(LOG_INFO + ">>>", "servName : " + name);
							/* Publish RAOP service */
							final ServiceInfo airPlayServiceInfo = ServiceInfo
									.create(AirPlayConstant.type, name, /* 服务名 */
											AirPlayConstant.port,
											0 /* weight */, 0 /* priority */,
											AirPlayConstant.getTxtHead());
							jmDNS.registerService(airPlayServiceInfo);
							Log.w(LOG_INFO + ">>>>",
									"Registered AirPlay service '"
											+ airPlayServiceInfo.getName()
											+ "' on " + "");
						} catch (final Throwable e) {
							Log.w(LOG_INFO
									+ " 错误：>>>Failed to publish service on "
									+ "", e.getMessage() + "<<<");
						}
//					}
//				}
//			} catch (SocketException e) {
//				Log.w(LOG_INFO + " 错误：>>>", e.getMessage() + "<<<");
//				e.printStackTrace();
//			}
//		}
	}

	/**
	 * 关闭所有的jmdns.
	 * @throws IOException 
	 */
	public void onShutdown() throws IOException {
		
		/* Close channels */
		final ChannelGroupFuture allChannelsClosed = channelGroup.close();

		/* Stop all mDNS responders */
		synchronized (s_jmDNSInstances) {
			for (final JmDNS jmDNS : s_jmDNSInstances) {
				try {
					jmDNS.unregisterAllServices();
					Log.w(LOG_INFO + ">>>", "Unregistered all services on "
							+ jmDNS.getInterface());
				} catch (final IOException e) {
					Log.w(LOG_INFO + " 错误: >>>",
							"Failed to unregister some services");
				}
			}
		}

		/* Wait for all channels to finish closing */
		allChannelsClosed.awaitUninterruptibly();
	}

	public static void stopPhoto() {
		if (Utils.ch != null) {
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
					HttpMethod.POST, "/event");
			request.addHeader("Content-Type", "text/x-apple-plist+xml");
			request.addHeader("Content-Length", 277);
			request.addHeader("X-Apple-Session-ID", ""
					+ Utils.X_APPLE_SESSION_ID);
			/* 正文 */
			ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
			try {
				channelBuffer.writeBytes(Constant.STOP_PHOTO_SESSION
						.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			request.setContent(channelBuffer);
			Utils.ch.write(request);
		}
	}

}
