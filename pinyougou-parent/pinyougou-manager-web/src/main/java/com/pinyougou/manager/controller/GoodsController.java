package com.pinyougou.manager.controller;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/*
	 * @Reference private ItemPageService itemPageService;
	 */

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination queueSolrDestination;

	@Autowired
	private Destination queueSolrDeleteDestination;

	@Autowired
	private Destination topicPageDestination;

	@Autowired
	private Destination topicPageDeleteDestination;// 用于删除静态网页的消息

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 增加
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods) {
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods) {
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id) {
		return goodsService.findOne(id);
	}

	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long[] ids) {
		try {
			goodsService.delete(ids);

			// 同步删除solr库中goods数据
			// 將ids出入消息队列pinyougou_queue_solr_delete
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {

				@Override
				public Message createMessage(Session session) throws JMSException {
					// Number实现了序列化,所以可以传输Object对象
					return session.createObjectMessage(ids);
				}
			});

			// 删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
		return goodsService.findPage(goods, page, rows);
	}

	// 更改审核状态
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {
			goodsService.updateStatus(ids, status);

			// 审核通过的数据导入solr：按照 SPU ID 查询 SKU 列表(状态为 1)
			if (status.equals("1")) {// 审核通过

				// 审核通过,生成静态详情页面
				/*
				 * for (int i = 0; i < ids.length; i++) {
				 * itemPageService.genItemHtml(ids[i]); }
				 */
				// 静态页生成
				for (final Long goodsId : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(goodsId + "");
						}
					});
				}
				// **********************************************************
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);

				// 调用搜索接口实现数据批量导入solr
				if (itemList.size() > 0) {
					// itemSearchService.importList(itemList);

					// 转换为JSON字符串
					final String itemListStr = JSON.toJSONString(itemList);

					// 将itemList存消息队列queueSolrDestination
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {

						@Override
						public Message createMessage(Session session) throws JMSException {
							TextMessage textMessage = session.createTextMessage(itemListStr);
							return textMessage;
						}
					});

				} else {
					System.out.println("没有明细数据");
				}
			}

			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}

	// 测试生成详情页面方法
	/*
	 * @RequestMapping("genHtml") public void genItemsHtml(Long goodsId) {
	 * 
	 * itemPageService.genItemHtml(goodsId);
	 * 
	 * }
	 */
}
