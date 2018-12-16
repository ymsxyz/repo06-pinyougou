package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * 
 * @author Administrator
 *
 */
public interface BrandService {

	public List<TbBrand> findAll();

	// 返回分页列表:参数(页码,每页记录数)
	public PageResult findPage(int pageNum, int pageSize);

	// 添加一个品牌
	public void addOne(TbBrand tbBrand);

	/*
	 * 修改品牌:
	 */
	// 通过id查询品牌信息:回显
	public TbBrand findOne(Long id);

	// 修改品牌:通过id定位是哪一个,传入新品牌
	public void update(TbBrand brand);

	// 删除
	public void delete(Long[] ids);

	// 分页搜索
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
	
	//品牌下拉框数据
	List<Map> selectOptionList();
}
