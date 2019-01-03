package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

/**
 * 监听类:获取消息实现业务:将itemList导入solr
 * 
 * @author ymsxyz
 *
 */
public class QueueSolrListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		// 将messag字符串转换为List集合
		TextMessage textMessage = (TextMessage) message;
		try {
			String text = textMessage.getText();
			List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
			
			itemSearchService.importList(itemList);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
