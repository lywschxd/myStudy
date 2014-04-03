package com.gimi.dlnagimi;

import java.io.File;
import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpRequest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.gimi.airplay.AirPlayCallBack;
import com.gimi.airplay.AirPlayMediaPlayer;
import com.gimi.airplay.VideoAsyncTask;
import com.gimi.caching.ImageCache;
import com.gimi.service.AirPlayServerRunnable;
import com.gimi.service.RAOPServerRunnable;
import com.gimi.utils.Utils;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 *  Airplay server for android 测试 demo.
 * 
 * ### AirPlay 协议说明 ###
 * http://nto.github.io/AirPlay.html#servicediscovery
 *
 * http://netty.io/ http://tool.oschina.net/apidocs/apidoc?api=netty
 * http://wenku
 * .baidu.com/link?url=jTCOrvulV4SZJR4G-eGz9AA22KhwJQE4B7yfqWj5DKgoSP
 * -HKqCNC6RX35B49DbnlZi_l8wNXx8fpQO9z1JMmyxfbPDSx0qruEobPm9liSy
 * 
 * @author hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
@EActivity(R.layout.activity_test)
public class TestActivity extends Activity {

	private RAOPServerRunnable raopServer;
	private AirPlayServerRunnable airPlayServer;
	private Context context;
	private AirPlayCallBack cb = null;

	@ViewById
	ImageView imageView1;
	@ViewById
	VideoView videoView1;
	@ViewById
	Button button1;

	@AfterViews
	public void initWidgets() {

		context = getApplicationContext();

		Utils.createSystemDir(context); // 必须创建的目录.

		videoView1.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				AirPlayMediaPlayer.state = AirPlayMediaPlayer.STATE_PLAYING;
			}
		});

		cb = new AirPlayCallBack() {
			@Override
			public void downImageOver(File file, HttpRequest request) {
				super.downImageOver(file, request);
				if (Utils.isCurrentShowPhotos(request)) {
					Log.w(">>>>>>>>>>>>>", "<<<<<<<<<<<加载图片!!!!");
					Toast.makeText(context, "加载图片" + file.getPath(),
							Toast.LENGTH_LONG).show();
					Bitmap bm = BitmapFactory.decodeFile(file.getPath());
					imageView1.setImageBitmap(bm);
				}
			}

			@Override
			public void stopPhotos() {
				AirPlayMediaPlayer.state = AirPlayMediaPlayer.STATE_STOPPED;
				videoView1.stopPlayback();
				super.stopPhotos();
			}

			@Override
			public void videoPlay(String url) {
				videoView1.setVideoURI(Uri.parse(url));
				videoView1.start();
				Toast.makeText(context, "加载图片" + url, Toast.LENGTH_LONG).show();
				super.videoPlay(url);
			}

			@Override
			public void videoSeek(double pos) {
				Log.w(">>>>>>>>>>>", "<<<<<<<<<<,跳转:" + pos * 1000 + "!!!");
				videoView1.seekTo((int) (pos * 1000)); // 0.001
				super.videoSeek(pos);
			}

			@Override
			public void videoPause(String state) {
				if (state.contains(AirPlayCallBack.VIDEO_STATE_PAUSE)) {
					Log.w(">>>>>>>>>", "<<<<<<<<<<<<暂停!!");
					AirPlayMediaPlayer.state = AirPlayMediaPlayer.STATE_PAUSED;
					videoView1.pause();
				} else {
					Log.w(">>>>>>>>>>", "<<<<<<<<<<<<<继续播放!!");
					AirPlayMediaPlayer.state = AirPlayMediaPlayer.STATE_PLAYING;
					videoView1.start();
				}
				super.videoPause(state);
			}

			@Override
			public double getVideoDuration() {
				AirPlayMediaPlayer.duration = ((double) videoView1
						.getDuration()) * 0.001;
				return AirPlayMediaPlayer.duration < 0.0 ? 0.0
						: AirPlayMediaPlayer.duration;
			}

			@Override
			public double getVideoPostion() {
				AirPlayMediaPlayer.position = ((double) videoView1
						.getCurrentPosition()) * 0.001;
				return AirPlayMediaPlayer.position < 0.0 ? 0.0
						: AirPlayMediaPlayer.position;
			}

		};
		// Utils.servName = "邱海泷@测试AirPlay";
		airPlayServer = new AirPlayServerRunnable(cb);
		raopServer = new RAOPServerRunnable();
		new Thread(airPlayServer).start(); /* 开启 AirPlay 服务 */
		new Thread(raopServer).start(); /* 开启 Raop 服务 */
	}

	@AfterInject
	public void initDatas() {
		new VideoAsyncTask(null, null).execute(); // 初始化一个空的 asynctask.
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			airPlayServer.onShutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
		raopServer.onShutdown();
	}

	@Click
	void button1() {
		File mediaFilesDir = context.getDir("mediaFiles",
				Context.MODE_WORLD_READABLE);

		Log.w("断开连接!!", "断开连接!!");
		airPlayServer.stopPhoto();
	}

	@Override
	protected void onStop() {
		ImageCache.clearCacheFiles(); // 清空目录下的所有文件.
		super.onStop();
	}
}
