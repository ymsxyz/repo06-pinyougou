package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.core.ParseException;
import freemarker.ext.beans.DateModel;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	// 读取路径配置文件
	@Value("${pagedir}")
	private String pagedir;

	// 读取配置文件的freemarkerConfig
	@Autowired
	private FreeMarkerConfig freemarkerConfig;

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 生成商品详细页
	 * 
	 * @param goodsId
	 * @return 是否成功生成页面
	 */
	@Override
	public boolean genItemHtml(Long goodsId) {
		// TODO Auto-generated method stub
		try {
			// 引入配置文件
			Configuration configuration = freemarkerConfig.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");

			Map dataModel = new HashMap<>();
			// 1查询商品
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			// 2.查询商品详细
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			// 3.在itemCat表查询goods表分级名称
			String category1 = itemCatMapper
					.selectByPrimaryKey(goodsMapper.selectByPrimaryKey(goodsId).getCategory1Id()).getName();
			String category2 = itemCatMapper
					.selectByPrimaryKey(goodsMapper.selectByPrimaryKey(goodsId).getCategory2Id()).getName();
			String category3 = itemCatMapper
					.selectByPrimaryKey(goodsMapper.selectByPrimaryKey(goodsId).getCategory3Id()).getName();
			dataModel.put("category1", category1);
			dataModel.put("category2", category2);
			dataModel.put("category3", category3);
			// 4.查询sku(item)表
			TbItemExample itemExample = new TbItemExample();
			Criteria criteria = itemExample.createCriteria();
			criteria.andStatusEqualTo("1");// 商品审核通过
			criteria.andGoodsIdEqualTo(goodsId);
			itemExample.setOrderByClause("is_default desc");// 按照状态降序，保证第一个为默认
			// 存入map集合
			List<TbItem> itemList = itemMapper.selectByExample(itemExample);
			dataModel.put("itemList", itemList);

			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pagedir + goodsId + ".html"), "utf-8");
			template.process(dataModel, out);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 删除商品详细页
	 * 
	 * @param goodsId
	 * @return
	 */
	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
			for (Long goodsId : goodsIds) {
				new File(pagedir + goodsId + ".html").delete();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
