package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional // 开启声明式事务
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {

		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		System.out.println("===" + page.getResult().get(0).getIsDelete());
		return new PageResult(page.getTotal(), page.getResult());
	}

	// 满足goods条件的分頁查询
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();// null:表示未被删除

		if (goods != null) {
			/*
			 * 模糊查询,可能bug:百度-->百度123,会认为是同一家商店 改为通过商店名(sellerId)精确查询
			 */

			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				// criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}

		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加:商品+商品大字段描述表
	 */
	@Override
	public void add(Goods goods) {

		goods.getGoods().setAuditStatus("0");// 设置商品初始状态
		goodsMapper.insert(goods.getGoods());

		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());// 设置商品描述表id:主键
		System.out.println(goods.getGoods());
		goodsDescMapper.insert(goods.getGoodsDesc());

		saveItemList(goods);// 插入商品 SKU 列表数据
	}

	/**
	 * 插入 SKU 列表数据
	 * 
	 * @param goods
	 */
	private void saveItemList(Goods goods) {
		// 是否启用规格
		// 如果启用
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				// 构建标题 SPU名称+ 规格选项值
				String title = goods.getGoods().getGoodsName();// SPU名称
				Map<String, Object> map = JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);

				setItemValues(item, goods);

				itemMapper.insert(item);
			}
		} else {// 没有启用规格

			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());// 标题
			item.setPrice(goods.getGoods().getPrice());// 价格
			item.setNum(99999);// 库存数量
			item.setStatus("1");// 状态
			item.setIsDefault("1");// 默认
			item.setSpec("{}");// 规格

			setItemValues(item, goods);

			itemMapper.insert(item);
		}
	}

	private void setItemValues(TbItem item, Goods goods) {

		// 商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());// 三级分类ID
		item.setCreateTime(new Date());// 创建日期
		item.setUpdateTime(new Date());// 更新日期

		item.setGoodsId(goods.getGoods().getId());// 商品ID
		item.setSellerId(goods.getGoods().getSellerId());// 商家ID

		// 分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		// 商家名称(店铺名称)
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		// 图片
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			item.setImage((String) imageList.get(0).get("url"));
		}

	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods) {

		goods.getGoods().setAuditStatus("0");// 设置未申请状态:如果是经过修改的商品，需要重新 设置状态
		goodsMapper.updateByPrimaryKey(goods.getGoods());// 保存商品表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());// 保存商品扩
		// ?删除原有的 sku 列表数据
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		// 添加新的 sku 列表数据
		saveItemList(goods);// 插入商品 SKU 列表数据
	}

	/**
	 * 根据ID获取实体:组合类
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id) {
		Goods goods = new Goods();

		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);

		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		// 查询sku商品列表
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		// 查询条件:商品id
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量删除:逻辑删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	// 修改商品状态
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);

			// 更新item表Status,于goods表的audit_status保持一致
			/*
			 * TbItemExample itemExample = new TbItemExample();
			 * com.pinyougou.pojo.TbItemExample.Criteria itemCriteria =
			 * itemExample.createCriteria(); itemCriteria.andGoodsIdEqualTo(id);
			 * 
			 * //更改对应item的状态 List<TbItem> itemList =
			 * itemMapper.selectByExample(itemExample);//查出对应item for (TbItem
			 * item : itemList) { item.setStatus(status);
			 * itemMapper.updateByExample(item, example1); }
			 */

		}

	}

	/**
	 * 根据商品 ID 和状态查询 Item表信息
	 * 
	 * @param goodsId
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}
}
