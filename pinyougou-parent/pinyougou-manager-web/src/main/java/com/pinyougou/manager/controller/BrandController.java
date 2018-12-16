package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;

	@RequestMapping("/findAll")
	public List<TbBrand> findAll() {
		return brandService.findAll();
	}

	// 分页查询
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {

		return brandService.findPage(page, rows);
	}

	// 添加一个
	@SuppressWarnings("finally")
	@RequestMapping("/add")
	public Result addOne(@RequestBody TbBrand tbBrand) {
		Result result = new Result();
		try {
			brandService.addOne(tbBrand);
			result.setSuccess(true);
			result.setmessage("添加成功");
		} catch (Exception e) {
			e.getStackTrace();
			result.setSuccess(false);
			result.setmessage("添加失败");
		} finally {
			return result;
		}
	}

	// 修改
	// 通过id查询品牌
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {

		TbBrand brand = brandService.findOne(id);
		return brand;
	}

	// 通过id定位修改品牌
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		Result result = new Result();
		try {
			brandService.update(brand);
			result.setSuccess(true);
			result.setmessage("修改成功");
		} catch (Exception e) {
			e.getStackTrace();
			result.setSuccess(false);
			result.setmessage("修改失败");
		} finally {
			return result;
		}
	}

	// 批量删除
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand, int page, int rows) {
		return brandService.findPage(brand, page, rows);
	}
	
	//下拉列表框
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList() {
		return brandService.selectOptionList();
	}
}
