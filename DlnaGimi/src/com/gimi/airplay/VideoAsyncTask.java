package com.gimi.airplay;

import android.os.AsyncTask;

public class VideoAsyncTask extends AsyncTask<Void, Void, String[]> {
	
	private String[] str = null;
	private AirPlayCallBack cb  = null;
	
	public VideoAsyncTask(String[] videos, AirPlayCallBack cb) {
		this.str = videos;
		this.cb = cb;
	}
	
	@Override
	protected String[] doInBackground(Void... params) {
		return str;
	}
	
	@Override
	protected void onPostExecute(String[] strArgs) {
		
		if (strArgs != null && this.cb != null) {
			if (strArgs[0].equals(AirPlayCallBack.VIDEO_STATE_STAING)) {
				String url = strArgs[1];
				cb.videoPlay(url);
				String pos = strArgs[2];
				cb.videoSeek(Double.valueOf(pos != null ? pos : "0.0"));
			} else if (strArgs[0].equals(AirPlayCallBack.VIDEO_STATE_PAUSEING)) {
				cb.videoPause(strArgs[1]);
			} else if (strArgs[0].equals(AirPlayCallBack.VIDEO_STATE_SEEK_POS)) {
				cb.videoSeek(AirPlayMediaPlayer.position);
			}
		}
		
		super.onPostExecute(str);
	}

}
