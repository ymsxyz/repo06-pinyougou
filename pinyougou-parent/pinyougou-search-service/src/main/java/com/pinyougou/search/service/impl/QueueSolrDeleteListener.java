package com.pinyougou.search.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;

import com.pinyougou.search.service.ItemSearchService;

/**
 * 监听类:获取消息实现业务:将itemList导入solr
 * 
 * @author ymsxyz
 *
 */
public class QueueSolrDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message message) {
		// 将messag对象转换为list
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			Long[] goodsIds = (Long[]) objectMessage.getObject();
			System.out.println("ItemDeleteListener 监听接收到消息..." + goodsIds);
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
			System.out.println("成功删除索引库中的记录");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
