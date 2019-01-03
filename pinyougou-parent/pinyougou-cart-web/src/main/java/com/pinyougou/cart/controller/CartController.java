package com.pinyougou.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	@Reference
	private CartService cartService;

	/**
	 * 从cookie或redis中获取购物车列表
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();// 获取用户名
		List<Cart> cartList_cookie = new ArrayList<>();
		if (username.equals("anonymousUser")) {// 未登录

			String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
			if (cartListString == null || cartListString.equals("")) {
				cartListString = "[]";// 不存在则为空对象
			}
			cartList_cookie = JSON.parseArray(cartListString, Cart.class);
			return cartList_cookie;
		} else {// 已登录
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			if (cartList_cookie.size() > 0) {// 如果本地存在购物车
				// 合并购物车
				cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
				// 清除本地 cookie 的数据
				util.CookieUtil.deleteCookie(request, response, "cartList");
				// 将合并后的数据存入 redis
				cartService.saveCartListToRedis(username, cartList_redis);
			}
			return cartList_redis;
		}

	}

	/**
	 * 添加商品到购物车
	 * 
	 * @param request
	 * @param response
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId, Integer num) {

		try {
			List<Cart> cartList = findCartList();// 获取购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			String username = SecurityContextHolder.getContext().getAuthentication().getName();// 获取用户名
			System.out.println("username:" + username);
			if (username.equals("anonymousUser")) {// 未登录
				// 将购物车列表放入缓存
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24,
						"UTF-8");
				return new Result(true, "添加到cookie成功");
			} else {// 已登录
				cartService.saveCartListToRedis(username, cartList);
				return new Result(true, "添加到redis成功");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
