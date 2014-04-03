package com.gimi.service;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import android.annotation.SuppressLint;
import android.util.Log;

import com.gimi.airtunes.AirTunesConstant;
import com.gimi.utils.Utils;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 
 * @描述: RaopServer 主要用于开启 AirTunes 服务，
 * 这个服务主要用于处理音频.
 * 详情参考(http://nto.github.io/AirPlay.html#introduction).
 * (有一些代码摘自 DroidAirPlay代码)
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class RAOPServerRunnable implements Runnable {
	public static final String LOG_INFO = RAOPServerRunnable.class.getName();
	static final Logger LOGGER = Logger.getLogger(RAOPServerRunnable.class.getName());
	private static final List<JmDNS> s_jmDNSInstances = new java.util.LinkedList<JmDNS>();
	
	public static ChannelGroup channelGroup = new DefaultChannelGroup();
	
	public static ExecutionHandler channelExecutionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 0, 0));
	public static ExecutorService executorService;
	
	public void run() {
		/* 确保服务是提前或者已经关闭的，不然就被占用了！！ */
//    	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Log.w(LOG_INFO + ">>>>", "提前关闭服务!!<<<<<");
//				onShutdown();
//			}
//    	}));
    	
//		startService();
		startDNSService();
	}

	void startService() {
		executorService = Executors.newCachedThreadPool();
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(executorService, executorService);
		ServerBootstrap serverBootstrap = new ServerBootstrap(channelFactory);
//		serverBootstrap.setPipelineFactory(new ());
		serverBootstrap.setOption("reuseAddress", true);
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);
		
		try {
			channelGroup.add(serverBootstrap.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), AirTunesConstant.port)));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.w(LOG_INFO + ">>>>", e.getMessage() + "<<<");
		}
		
	}
	
	@SuppressLint("NewApi")
	void startDNSService() {
//		/* Create mDNS responders. */
//		synchronized (s_jmDNSInstances) {
//			try {
//				for (final NetworkInterface iface : Collections
//						.list(NetworkInterface.getNetworkInterfaces())) {
//
//					if (iface.isLoopback())
//						continue;
//
//					if (iface.isPointToPoint())
//						continue;
//
//					if (!iface.isUp())
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
							final ServiceInfo airTunesServiceInfo = ServiceInfo
									.create(AirTunesConstant.type, name, /* 服务名 */
											AirTunesConstant.port,
											0 /* weight */, 0 /* priority */,
											AirTunesConstant.getTxtHead());
							jmDNS.registerService(airTunesServiceInfo);
							Log.w(LOG_INFO + ">>>>",
									"Registered AirTunes service '"
											+ airTunesServiceInfo.getName()
											+ "' on " + "");
						} catch (final Throwable e) {
							Log.w(LOG_INFO + "Failed to publish service on "
									+ "", e.getMessage());
						}
//					}
//				}
//			} catch (SocketException e) {
//				Log.w(LOG_INFO + "=Error==", "===Error=");
//				e.printStackTrace();
//			}
//		}
	}
	
	public static void onShutdown() {
		/* Close channels */
		final ChannelGroupFuture allChannelsClosed = channelGroup.close();

		/* Stop all mDNS responders */
		synchronized(s_jmDNSInstances) {
			for(final JmDNS jmDNS: s_jmDNSInstances) {
				try {
					jmDNS.unregisterAllServices();
					Log.w(LOG_INFO + ">>>", "Unregistered all services on " + jmDNS.getInterface());
				}
				catch (final IOException e) {
					Log.w(LOG_INFO + ">>>", "Failed to unregister some services");
				}
			}
		}

		/* Wait for all channels to finish closing */
		allChannelsClosed.awaitUninterruptibly();
		
		/* Stop the ExecutorService */
//		ExecutorService.shutdown();

		/* Release the OrderedMemoryAwareThreadPoolExecutor */
//		ChannelExecutionHandler.releaseExternalResources();
	}
	
	public static ChannelGroup getChannelGroup() {
		return channelGroup;
	}
	
	/**
	 * 
	 * @return
	 */
	public static ChannelHandler getChannelExecutionHandler() {
		return channelExecutionHandler;
	}
	
	public static ExecutorService getExecutorService() {
		return executorService;
	}
}
