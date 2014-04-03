package com.gimi.xml;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 
 * @描述: 保存XML节点.
 * 
 * @author     hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class PlistMode {
	DictMode dictMode = new DictMode();
	
	public PlistMode() {
		/* 例子.
		dictMode.map.put("settings", new DictMode());
		((DictMode)dictMode.map.get("settings")).map.put("SlideDuration", "3");
		((DictMode)dictMode.map.get("settings")).map.put("theme", "Classic");
		dictMode.map.put("state", "Playing");
		*/
	}
	
}
