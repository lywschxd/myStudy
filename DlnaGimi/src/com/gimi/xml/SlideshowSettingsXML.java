package com.gimi.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Copyright (C) 2014 极米科技有限公司, Inc
 * 
 * @描述: slideshow settings XML的解析. [key]:类型
 * @author hailongqiu <356752238@qq.com>
 * @Maintainer hailongqiu <356752238@qq.com>
 */
public class SlideshowSettingsXML {
	private static final String LOG_INFO = SlideshowSettingsXML.class.getName();

	Document document = null;
	DocumentBuilderFactory factory = null;
	InputStream inputStream = null;
	DocumentBuilder builder = null;

	PlistMode plistMode = new PlistMode(); // 保存XML数据.

	public SlideshowSettingsXML(InputStream stream) {

		/* XML文件 */
		factory = DocumentBuilderFactory.newInstance();
		/* 保存流信息 */
		inputStream = stream;

		try {
			/* 加载文档 */
			builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);
			/* 根 Element */
			Element root = document.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("plist");
			
			/* 遍历根节点所有子节点 */
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NodeList properties = node.getChildNodes();
				
				/* 遍历根节点所有子节点 */
				if (node.getNodeName().equals("dict")) {
					getNodesValues(properties, plistMode.dictMode.map);
				}
				
			}

		} catch (ParserConfigurationException e) {
			Log.w(LOG_INFO + ">>>", "<<<" + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.w(LOG_INFO + ">>>", "<<<" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.w(LOG_INFO + ">>>", "<<<" + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 遍历 dict 下的所有子节点.
	 * 
	 * @param nodes
	 * @param map
	 */
	void getNodesValues(NodeList nodes, HashMap<String, Object> map) {
		/* 所有子节点 */
		for (int j = 0; j < nodes.getLength(); j++) {
			Node property = nodes.item(j);
			String nodeName = property.getNodeName();
			String nodevalue = property.getNodeValue();

			if (nodeName.equals("key")) {
				j++;
				Node keyProperty = nodes.item(j);
				String keyValue = keyProperty.getNodeValue();

				if (keyProperty.getNodeName().equals("dict")) {
					DictMode dictMode = new DictMode();
					map.put("" + nodevalue, dictMode);
					getNodesValues(keyProperty.getChildNodes(),
							((DictMode) map.get("" + nodevalue)).map);
				} else {
					map.put("" + nodevalue, keyValue);
				}

			} else if (nodeName.equals("dict")) {
				getNodesValues(property.getChildNodes(), plistMode.dictMode.map);
			}

		}

	}

}

/*
 * [大概内容]
 * 
 * <?xml version="1.0" encoding="UTF-8"?> <!DOCTYPE plist PUBLIC
 * "-//Apple//DTD PLIST 1.0//EN"
 * "http://www.apple.com/DTDs/PropertyList-1.0.dtd"> 
 * <plist version="1.0">
 * 
 * 	<dict>
 * 		 
 * 	     <key>settings</key> 
 * 		 <dict> 
 * 		 	<key>slideDuration</key>
 * 		 	<integer>3</integer> 
 * 			<key>theme</key>
 * 			<string>Classic</string> 
 * 		 </dict>  # key : dict.
 * 			
 * 		 <key>state</key> 
 * 		 <string>playing</string>  # key : string
 * 	</dict> 
 *
 * </plist>
 */


