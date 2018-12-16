package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
	}

	// 分页查询
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		// 使用MyBatis分页插件
		PageHelper.startPage(pageNum, pageSize);
		// 查询所有
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		// 返回总记录数,结果集
		return new PageResult(page.getTotal(), page.getResult());
	}

	// 添加一个
	@Override
	public void addOne(TbBrand tbBrand) {
		brandMapper.insert(tbBrand);
	}

	/*
	 * 修改品牌:
	 */
	// 通过id查询品牌信息:回显
	public TbBrand findOne(Long id) {
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(id);
		return tbBrand;
	};

	// 修改品牌:通过id定位是哪一个
	public void update(TbBrand brand) {
		// 1.不会
		brandMapper.updateByPrimaryKey(brand);
	};

	// 批量删除
	public void delete(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	// 分页搜索
	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();
		if (brand != null) {
			if (brand.getName() != null && brand.getName().length() > 0) {
				criteria.andNameLike("%" + brand.getName() + "%");
			}
			if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
				criteria.andFirstCharEqualTo(brand.getFirstChar());
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	//下拉列表框
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}
}
