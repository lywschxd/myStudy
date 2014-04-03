package com.gimi.airplay;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import android.annotation.SuppressLint;
import android.util.Log;

import com.gimi.caching.ImageCache;
import com.gimi.plist.BPDict;
import com.gimi.plist.BPItem;
import com.gimi.plist.BinaryPlist;
import com.gimi.plist.BinaryPlistException;
import com.gimi.utils.Constant;
import com.gimi.utils.Utils;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * 添加一个Airplay服务 图片的Handler.
 * 
 * @author hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
@SuppressLint("NewApi")
public class AirPlayPhotosHeaderHandler extends SimpleChannelUpstreamHandler {
	private final String LOG_INFO = AirPlayPhotosHeaderHandler.class.getName();

	ImageCache imageCache = new ImageCache();

	AirPlayCallBack cb = null;

	boolean send_event_bool = false;
	boolean play_bool = false;
	
	public AirPlayPhotosHeaderHandler(AirPlayCallBack cb) {
		this.cb = cb;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		if (ctx == null || e == null) {
			Log.w(LOG_INFO + ">>>>>>>>>>>>>>>>", "<<<<<<<<<<<<<<<"
					+ "ctx, e为空!!");
			return;
		}

		HttpRequest request = (HttpRequest) e.getMessage();
		Channel ch = e.getChannel();
		String content = request.toString();
		String date = Utils.dateToGMT();

		Utils.ch = ch; // 保存会话.
		Utils.X_APPLE_SESSION_ID = request.getHeader("X-Apple-Session-ID"); // 保存会话ID.

//		Log.e(LOG_INFO + ">>>>>>>>>>>", "<<<<<<<<<<<<<<<<<<<<<<< ");
//		Log.e(LOG_INFO + ">>>>全部: ", request.toString() + "<<< ");

		if (content.contains("/reverse")/* AirPlay链接确认 */) {
			sendReverse(ch, date); // 向控制点发送链接确认.
		} else if (content.contains("/slideshow-features"/* Photo传送确认 */)) {
			sendSlidShowFeatures(ch, date); // 向控制点发送接收图片确认.
		} else if (content.contains("/photo")/* 图片 */) {
			/**
			 * 这里需要注意，在显示一张的图片的时候，控制端(苹果设备)会发送图片的数据，
			 * 在接收完毕后，如果控制端没有作任何的操作的话，控制端那边会将前后的图片数据 也会传送过来，带有
			 * X-Apple-AssetAction: cacheOnly 字样的. 参考:
			 * http://nto.github.io/AirPlay.html#photos-photocaching
			 * */
			/* 接收控制点的数据保存成图片. */
			receivePhoToLocal(request);
			/* 向控制点发送接收图片完毕的确认信息. */
			sendPhotoOk(ch);
		} else if (request.getUri().equals("/stop"/* 停止 */)) {
			play_bool = false;
			/* 停止正在播放的视频或者图片 */
			sendPhotoStop(ch, date);
		} else if (request.getUri().equals("/server-info")) {
			sendServerInfoOK(ch, date);
		} else if (request.getUri().equals("/play")) {
			play_bool = true;
			/* 获取播放地址和位置 */
			getPlayInfo(request);
			/* 发送确认信息 */
			sendPlayOK(ch, date);
			setPlayConnect();
			send_event_bool = true;
		} else if (request.getUri().equals("/setProperty?forwardEndTime")) {
			sendSetProperty404(ch);
		} else if (request.getUri().equals("/setProperty?reverseEndTime")) {
			sendSetProperty404(ch);
		} else if (request.getUri().contains("/rate?value=")) {
			/* 获取rate的值并保存起来 */
			saveRateValue(request.getUri());
			/* 发送rate的确认信息 */
			sendRateOK(ch, date);

			/* event 发送 确认 */
			if (send_event_bool) {
				send_event_bool = false; // 防止被重复发送多次 event 信息.
				sendVideoEventOK(ch, date);
			}

			setRateConnect();
		} else if (request.getUri().contains("/playback-info")) {
			/* 防止没有在播放的期间返回这些无用的垃圾信息,导致死循环的发送!!!! */
			if (play_bool) {
				sendPlayBackInfoOK(ch, date);
			}
		} else if (request.getUri().contains("/scrub") && request.getMethod().equals(HttpMethod.GET)) {
			sendScrubOK(ch, date);
		} else if (request.getUri().contains("/scrub") && request.getMethod().equals(HttpMethod.POST)) {
			/* 保存当前苹果设备的播放进度 */
			saveScrubPostionValue(request);
			/* 设置链接事件 */
			setScrubSeekConnect();
			/* 发送确认信息 */
			sendPostScrubPostion(ch, date);
		}

	}

	ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			Channel ch = future.getChannel();
		}
	};

	/**
	 * 向控制点发送链接确认.
	 */
	void sendReverse(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.SWITCHING_PROTOCOLS);
		response.addHeader("Date", "" + date);
		response.addHeader("Upgrade", "" + "PTTH/1.0");
		response.addHeader("Connection", "" + "Upgrade");
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 向控制端发送图片接受确认.
	 */
	void sendSlidShowFeatures(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		/* 头信息 */
		response.addHeader("Date", "" + date);
		/* 写入 */
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 接收控制点的数据保存成图片.
	 */
	void receivePhoToLocal(HttpRequest request) {
		ChannelBuffer data = request.getContent();
		String key = request.getHeader("X-Apple-AssetKey"); // 关键字(唯一标示).
		/* 将传过来的数据保存成图片 */
		try {
			imageCache.downloadImage(key, data, request, cb);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向控制点发送接收图片完毕的确认信息.
	 */
	void sendPhotoOk(Channel ch) {
		/* 回应客户端 */
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Content-Length", "0");
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 接收到客户端（苹果设备） 发送的停止会话， 回应确认.
	 */
	void sendPhotoStop(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Date", "" + date);
		response.addHeader("Content-Length", "0");
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 发送 server-info 确认信息.
	 */
	void sendServerInfoOK(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Content-Type", "text/x-apple-plist+xml");
		response.addHeader("Date", "" + date);
		response.addHeader("Content-Length", 425);
		response.setContent(ChannelBuffers.copiedBuffer(
				Constant.FETCH_SERVER_INFORMATIONS, CharsetUtil.UTF_8));
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 获取播放信息.
	 */
	void getPlayInfo(HttpRequest request) {
		/* 获取播放地址和位置 */
		String contentStr = request.getContent().toString(
				Charset.forName("UTF-8"));

		if (contentStr.contains("bplist")) { /* Plist二进制的解析. */
			BPItem root = null;
			try {
				root = BinaryPlist.decode(request.getContent().array());
				BPDict newDict = (BPDict) root;
				/* Content-Location Start-Position */
				AirPlayMediaPlayer.url = newDict.get("Content-Location", "");
				AirPlayMediaPlayer.position = newDict
						.get("Start-Position", 0.0);
			} catch (BinaryPlistException e1) {
				e1.printStackTrace();
			}
		} else { /* 这里一般都是 IPad的苹果设备才是正常的地址. */
			int index = contentStr.indexOf("Start-Position:");
			String url = contentStr.substring("Content-Location:".length(),
					index);
			AirPlayMediaPlayer.url = url;
			String pos = contentStr.substring(index
					+ "Start-Position:".length());
			AirPlayMediaPlayer.position = (pos != null ? Double.valueOf(pos)
					: 0.0);
		}

		/* 输出信息 */
		Log.w(LOG_INFO + ">>>>>>>>>>>>>>", "<<<<<<<<地址:"
				+ AirPlayMediaPlayer.url);
		Log.w(LOG_INFO + ">>>>>>>>>>>>>>", "<<<<<<<<位置:"
				+ AirPlayMediaPlayer.position);

	}

	/**
	 * 发送获取地址和位置的确认信息.
	 */
	void sendPlayOK(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Date", Utils.dateToGMT() + "");
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 发送播放事件.
	 */
	void setPlayConnect() {
		String[] str = new String[8];
		str[0] = AirPlayCallBack.VIDEO_STATE_STAING; // 正准备播放.
		str[1] = AirPlayMediaPlayer.url;
		str[2] = AirPlayMediaPlayer.position + "";
		new VideoAsyncTask(str, cb).execute();
	}

	/**
	 * 发送404错误.
	 */
	void sendSetProperty404(Channel ch) {
		/* 按照海美迪的捉包协议，返回 404 Not Found */
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.NOT_FOUND);
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 保存 rate 的值.
	 * 
	 * @param uri
	 */
	void saveRateValue(String uri) {
		String value = uri.substring("/rate?value=".length());

		if (value != null) {
			value = value.trim();
		}

		try {
			AirPlayMediaPlayer.rate = value;
		} catch (Exception e) {
			AirPlayMediaPlayer.rate = AirPlayMediaPlayer.RATE_ERROR_STATE;
			Log.w(LOG_INFO + ">>>>>>>>>>>>",
					"<<<<<<<<<<<<<转换错误 rate : " + e.getMessage());
		}

	}

	/**
	 * 向 rate 发送 确认信息.
	 */
	void sendRateOK(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Date", date);
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 发送 /event 给客户端.
	 */
	void sendVideoEventOK(Channel ch, String date) {
		HttpRequest requestEvent = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/event");
		requestEvent.headers().set("Content-Type", "application/x-apple-plist");
		requestEvent.headers().set("Content-Length", "" + Constant.VIDEO_EVNET_INFO.getBytes().length);
		requestEvent.headers().set("X-Apple-Session-ID", "" + Utils.X_APPLE_SESSION_ID);
		/* 设置内容 */
		requestEvent.setContent(ChannelBuffers.copiedBuffer(
				Constant.VIDEO_EVNET_INFO, CharsetUtil.UTF_8));
//		ChannelFuture futrue = ch.write(requestEvent);
		/* 发送内容 */
//		futrue.addListener(remover);
//		Log.w(LOG_INFO + ">>>>>>>>", "<<<<<<<<" + "/event 发送成功!!");
	}

	/**
	 * 发送暂停和继续播放的事件设置.
	 */
	void setRateConnect() {
		String[] strArgs = new String[10];
		strArgs[0] = AirPlayCallBack.VIDEO_STATE_PAUSEING;
		strArgs[1] = AirPlayMediaPlayer.rate + "";
		new VideoAsyncTask(strArgs, cb).execute();
	}

	/**
	 * 向客户端发送 playback info 确认信息.
	 */
	void sendPlayBackInfoOK(Channel ch, String date) {
//		Log.w(LOG_INFO + ">>>>>>>>", AirPlayMediaPlayer.rate.substring(0) + "<<<<<<<<<<" + "duration:" + cb.getVideoDuration() + "-pos:" + cb.getVideoPostion());
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		/* 头信息 */
		response.addHeader("Date", date);
		response.addHeader("Content-Type", "text/x-apple-plist+xml");
		response.addHeader("Content-Length",
				"" + Constant.PLAYBACK_INFO.getBytes().length);
		/* 设置 playback info 内容 xml */
		response.setContent(ChannelBuffers.copiedBuffer(Constant.PLAYBACK_INFO,
				CharsetUtil.UTF_8));
		/* 发送 */
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
//		Log.w(LOG_INFO + ">>>>>>>", "<<<<<<<<<<" + "发送 playback-info 返回信息成功!!");
	}

	/**
	 * 将 duration 和 position 发送给客户端.
	 */
	void sendScrubOK(Channel ch, String date) {
		Log.w(LOG_INFO + ">>>>>>>>",
				"/scrub-<<<<<<<<" + "duration:" + cb.getVideoDuration()
						+ "-position:" + cb.getVideoPostion());
		String scrub_context = "" + "duration: " + AirPlayMediaPlayer.duration
				+ "\r\n" + "position: " + AirPlayMediaPlayer.position + "\r\n";
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Date", date);
		response.addHeader("Content-Type", "text/parameters");
		response.addHeader("Content-Length", scrub_context.getBytes().length);
		response.setContent(ChannelBuffers.copiedBuffer(scrub_context,
				CharsetUtil.UTF_8));
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}

	/**
	 * 保存 post scrub 的值.
	 */
	void saveScrubPostionValue(HttpRequest request) {
		String pos = request.getUri().substring("/scrub?position=".length());
		Log.w(LOG_INFO + "<<<<<<<<<<<<<", "<<<<<<<<<<<pos:" + pos);
		AirPlayMediaPlayer.position = pos != null ? Double.valueOf(pos.trim()) : 0.0;
	}
	
	/**
	 * 设置事件.
	 */
	void setScrubSeekConnect() {
		String[] strArgs = new String[10];
		strArgs[0] = AirPlayCallBack.VIDEO_STATE_SEEK_POS;
		new VideoAsyncTask(strArgs, cb).execute();
	}
	
	/**
	 * 发送 scrub 确认信息.
	 */
	void sendPostScrubPostion(Channel ch, String date) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.addHeader("Date", "" + date);
		ChannelFuture futrue = ch.write(response);
		futrue.addListener(remover);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

//		Log.w(LOG_INFO + ">>>", "exceptionCaught: " + e.toString() + "<<<");
		super.exceptionCaught(ctx, e);

		if (e.getCause() != null) {
			e.getCause().printStackTrace();
		}

		e.getChannel().close();
	}

}
