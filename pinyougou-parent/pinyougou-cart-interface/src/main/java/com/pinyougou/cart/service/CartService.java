package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojo.Cart;

public interface CartService {

	/**
	 * 根据详情id和数量num添加商品到购物车
	 * 
	 * @param CartList
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> CartList, Long itemId, Integer num);

	/**
	 * 从 redis 中查询购物车
	 * 
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);

	/**
	 * 将购物车保存到 redis
	 * 
	 * @param username
	 * @param cartList
	 */
	public void saveCartListToRedis(String username, List<Cart> cartList);
	
	/**
	 * 合并cookie数据redis
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
