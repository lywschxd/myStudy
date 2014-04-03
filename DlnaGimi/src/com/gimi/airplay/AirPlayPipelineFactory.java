/*
 * This file is part of AirReceiver.
 *
 * AirReceiver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * AirReceiver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with AirReceiver.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gimi.airplay;

import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;


/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * Factory for AirTunes/RAOP RTSP channels
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class AirPlayPipelineFactory implements ChannelPipelineFactory {
	AirPlayCallBack cb = null;
	
	public AirPlayPipelineFactory(AirPlayCallBack cb) {
		this.cb = cb;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		
		final ChannelPipeline pipeline = Channels.pipeline();
		ExecutionHandler executionHandler = new ExecutionHandler( 
	             new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));

		pipeline.addLast("decoder",    new HttpRequestDecoder());           // 解码过程.
		pipeline.addLast("encoder",    new HttpResponseEncoder());          // 解码过程.
		pipeline.addLast("aggregator", new HttpChunkAggregator(655360));    // 缓冲大小.
		pipeline.addLast("execution",  new ExecutionHandler(Executors.newCachedThreadPool()));
//		pipeline.addLast("execution",  executionHandler);
		pipeline.addLast("photos",     new AirPlayPhotosHeaderHandler(cb));   // 图片,视频 处理.
		
		return pipeline;
		
	}

}
