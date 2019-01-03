package com.pinyougou.page.service;

import java.util.List;

import com.pinyougou.pojo.TbItem;

public interface ItemPageService {

	/**
	 * @param goodsId
	 * @return 是否成功生成页面
	 */
	public boolean genItemHtml(Long goodsId);
	
	/**
	* 删除商品详细页
	* @param goodsId
	* @return
	*/
	public boolean deleteItemHtml(Long[] goodsIds);
}
