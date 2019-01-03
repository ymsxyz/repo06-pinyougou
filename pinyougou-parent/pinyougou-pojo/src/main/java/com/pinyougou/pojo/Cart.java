package com.pinyougou.pojo;

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author ymsxyz
 * 购物车类:包含商家id,商家名称,订单详情列表
 */
public class Cart implements Serializable{

	private String sellerId;
	private String sellerName;
	private List<TbOrderItem> orderItemList;
	public String getSellerId() {
		return sellerId;
	}
	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public List<TbOrderItem> getOrderItemList() {
		return orderItemList;
	}
	public void setOrderItemList(List<TbOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

}
