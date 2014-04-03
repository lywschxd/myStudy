package com.gimi.caching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;

import android.os.AsyncTask;
import android.util.Log;

import com.gimi.airplay.AirPlayCallBack;
import com.gimi.utils.Utils;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * @描述: 图片缓冲.(已经存在的就不需要重新写入了)
 * 
 * @author hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 ****/
public class ImageCache {

	private final String LOG_INFO = ImageCache.class.getName();
	private static String cacheDir = Utils.createAirPlayPhotosCache("/AirPlayCache");

	/**
	 * 设置缓冲目录.
	 * 
	 * @param dirPath
	 *            : 只需要传入 /AirPlayCache 这样的路径便可.
	 */
	public void setCacheDir(String dirPath) {
		cacheDir = Utils.createAirPlayPhotosCache(cacheDir);
		;
	}

	/**
	 * 获取缓冲目录.
	 */
	public String getCacheDir() {
		Log.w(LOG_INFO + ">>>>>>>>>", "<<<<<<<<缓冲目录:" + this.cacheDir);
		return this.cacheDir;
	}

	/**
	 * 获取当前目录下的所有文件.
	 */
	public File[] getCurrentCacheDirAllFiles() {
		return new File(this.cacheDir).listFiles();
	}

	/**
	 * 清空缓冲目录下的所有文件.
	 */
	public static void clearCacheFiles() {

		for (File file : new File(cacheDir).listFiles()) {

			if (file.exists() && file.isFile()
					&& file.getPath().endsWith("jpeg")) {
				file.delete();
			}

		}
		
		Log.w("<<<<<<<<<<<<", ">>>>>>>>>>清空缓冲目录成功!!");
	}

	/**
	 * 清空离现在时间之差的文件.
	 */
	public void clearTimeCacheFiles(int delday /* 需要删除的相差天数 */) {
		Date currentDate = new Date(System.currentTimeMillis()); // 当前时间.

		for (File file : new File(this.cacheDir).listFiles()) {
			Date fileDate = getFileTime(file);

			if (file.exists() && file.isFile()
					&& file.getPath().endsWith("jpeg")
					&& (currentDate.getDate() - fileDate.getDate()) > delday) {
				file.delete();
			}

		}

	}

	AirPlayCallBack cb  = null;
	
	/**
	 * 下载图片.
	 */
	public File downloadImage(String key, ChannelBuffer data, HttpRequest request, AirPlayCallBack cb)
			throws FileNotFoundException, IOException {
		this.cb = cb;
		new ImageAsyncTask(key, data, request).execute();
		return null;
	}
	
	private class ImageAsyncTask extends AsyncTask<Void, Integer, File> {
		
		ChannelBuffer data = null;
		String key = null;
		HttpRequest request = null;
		
		public ImageAsyncTask(String key, ChannelBuffer data, HttpRequest request) {
			this.key  = key;
			this.data = data;
			this.request   = request;
		}
		
		@Override
		protected File doInBackground(Void... params) {
			File file = null;
			
			try {
				file = serachFile(this.key);
				if (file == null) {
					File writeFile = new File(getCacheDir() + "/" + key + ".jpeg");
					byte[] dst = new byte[data.capacity()];
					data.getBytes(0, dst);
					FileOutputStream outStream = new FileOutputStream(
							writeFile.getPath());
					outStream.write(dst);
					outStream.flush();
					outStream.close();
					return writeFile;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return file;
		}
		
		@Override
		protected void onPostExecute(File result) {
			cb.downImageOver(result, this.request);
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * 根据关键字搜索是否有存在的文件. 有则返回File类型，否则返回NULL类型.
	 */
	public File serachFile(String key) throws FileNotFoundException,
			IOException {

		for (File file : getCurrentCacheDirAllFiles()) {
			/* 文件名类似 并且文件大小不能为零的,才返回File */
			if (file.getName().startsWith(key) && getFileSize(file) != 0) {
				return file;
			}

		}

		return null; /* 返回 null, 写入新文件 */
	}

	/**
	 * 获取文件大小
	 */
	public int getFileSize(File file) throws FileNotFoundException, IOException {
		return new FileInputStream(file).available();
	}

	/**
	 * 获取文件时间.
	 */
	public Date getFileTime(File file) {
		return new Date(file.lastModified());
	}

}
