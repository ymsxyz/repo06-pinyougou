package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.Result;
import com.ctc.wstx.io.AsciiReader;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;

@SuppressWarnings("all")
@Service(timeout = 100000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public Map<String, Object> search(Map searchMap) {
		Map<String, Object> map = new HashMap<>();

		// 多关键字空格处理:去掉空格
		String keywords = (String) searchMap.get("keywords");
		// 解决空指针异常
		if (keywords != null && keywords != "") {
			searchMap.put("keywords", keywords.replace(" ", ""));
		}

		// 1.关键字查询列表高亮显示
		map.putAll(searchList(searchMap));
		// 2.根据关键字查询商品分类
		List categoryList = searchCategory(searchMap);
		map.put("categoryList", categoryList);
		// 3.查询品牌和规格列表 searchMap:{spec=, category=, keywords=三星, brand=}
		String category = (String) searchMap.get("category");

		if (!category.equals("")) {// 如果搜索条件有分类字段.即用户点击某个分类

			map.putAll(searchBrandAndSpecList(category));

		} else {
			if (categoryList.size() > 0) {// 若有分类用户未点击,以第一个为准:如 手机 平板电视 以手机为准

				map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));

			}
		}

		return map;
	}

	// 1.根据关键字搜索列表
	private Map searchList(Map searchMap) {
		Map map = new HashMap();

		HighlightQuery query = new SimpleHighlightQuery();// 查询条件
		// 设置高亮域
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 高亮前缀
		highlightOptions.setSimplePostfix("</em>");// 高亮后缀
		query.setHighlightOptions(highlightOptions);// 设置高亮选项

		// 1.1 按照关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 1.2按照商品分类过滤
		if (!"".equals(searchMap.get("category"))) {// 如果分类(如手机,平板电视)存在
			FilterQuery filterQuery = new SimpleFilterQuery();// 创建分类过滤条件对象
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));// 判断item_category域是否与参数相同
			filterQuery.addCriteria(filterCriteria);// 过滤查询添加条件
			query.addFilterQuery(filterQuery);// 将过滤查询条件加入总查询
		}

		// 1.3按照品牌分类过滤
		if (!"".equals(searchMap.get("brand"))) {// 如果分类(如手机,平板电视)存在
			FilterQuery filterQuery = new SimpleFilterQuery();// 创建分类过滤条件对象
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);// 过滤查询添加条件
			query.addFilterQuery(filterQuery);// 将过滤查询条件加入总查询
		}
		// 1.4按照规格分类过滤:多个规格
		if (searchMap.get("spec") != null) {// 如果分类(如手机,平板电视)存在

			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");// 获取前端规格集合
			for (String key : specMap.keySet()) {// key:内存/网路...
				FilterQuery filterQuery = new SimpleFilterQuery();// 创建分类过滤条件对象
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));// 判断某个规格
				filterQuery.addCriteria(filterCriteria);// 过滤查询添加条件
				query.addFilterQuery(filterQuery);// 将过滤查询条件加入总查询
			}
		}

		// 1.5按照价格分类过滤
		if (!"".equals(searchMap.get("price"))) {// 如果分类价格(如0-500)存在
			String price = (String) searchMap.get("price");
			FilterQuery filterQuery = new SimpleFilterQuery();// 创建价格过滤条件对象
			// 定义最低-最高价格
			String lowerPrice = price.split("-")[0];
			String greaterPrice = price.split("-")[1];

			// 若区间低价不是0
			if (!lowerPrice.equals("0")) {
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(lowerPrice);
				filterQuery.addCriteria(filterCriteria);// 过滤查询添加条件
			}
			// 若区间高价不是*
			if (!greaterPrice.equals("*")) {
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(greaterPrice);
				filterQuery.addCriteria(filterCriteria);// 过滤查询添加条件
			}

			query.addFilterQuery(filterQuery);// 将过滤查询条件加入总查询
		}

		// 1.6分页查询
		// pageNo 当前页码 pageSize每页记录数
		Integer pageNo = (Integer) searchMap.get("pageNo");

		if (pageNo == null) {
			pageNo = 1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null) {
			pageSize = 20;
		}
		// 查询索引
		query.setOffset((pageNo - 1) * pageSize);
		// 查询记录数
		query.setRows(pageSize);

		// 1.7排序:new sort(排序方式,哪个域)
		String sortValue = (String) searchMap.get("sort");// ASC DESC
		String sortField = (String) searchMap.get("sortField");// 排序字段:通用
		// 如果用户点击某属性(如价格)排序
		if (sortValue != null && !sortValue.equals("")) {

			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Direction.ASC, "item_" + sortField);
				// 添加排序查询条件
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Direction.DESC, "item_" + sortField);
				// 添加排序查询条件
				query.addSort(sort);
			}

		}

		// *********获取高亮结果集:分页,默认20条*****************
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

		for (HighlightEntry<TbItem> h : page.getHighlighted()) {// 循环高亮入口集合
			TbItem item = h.getEntity();// 获取原实体类

			if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {

				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));// 设置高亮的结果
			}
		}

		map.put("rows", page.getContent());
		// System.out.println("resultMap" + map.toString());

		// 返回总页数
		map.put("totalPages", page.getTotalPages());

		// 返回总记录数
		map.put("total", page.getTotalElements());

		return map;
	}

	// 2.按照category查询分类列表
	private List searchCategory(Map searchMap) {

		List<String> list = new ArrayList<>();
		// 总查询条件
		Query query = new SimpleQuery();

		// 按照关键字查询:item_keywords复制域;keywords:搜索框内容
		// 标准1:关键字分词查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));// {keywords=华为}
		query.addCriteria(criteria);
		// 标准2:分组选项:item中category
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		// 添加条件
		query.setGroupOptions(groupOptions);

		// 得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 更具列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());// 分组结果的名称封装到返回值 getGroupValue:手机
			// System.out.println(entry.getGroupValue());
		}

		return list;

	}

	// 3.查询品牌和规格列表
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);// 获取模板ID

		if (typeId != null) {
			// 根据模板 ID 查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);// 返回值添加品牌列表
			// 根据模板 ID 查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	/**
	 * 导入数据
	 * 
	 * @param list
	 */
	@Override
	public void importList(List list) {

		solrTemplate.saveBeans(list);
		solrTemplate.commit();

	}

	/**
	 * 删除solr库item数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		System.out.println("删除商品 ID" + goodsIdList);

		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
