package com.pinyougou.content.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.common.TemplateAwareExpressionParser;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		
		Long categoryId = content.getCategoryId();
		redisTemplate.boundHashOps("content").delete(categoryId);//清除缓存
		contentMapper.insert(content);
	}

	/**
	 * 修改:传过来的content的category可能改变,id不变,数据库的还未改变;  首页轮播图-->其他图,则其他图条目类需要清理缓存
	 *    需要根据传来content的id查询数据库
	 */
	@Override
	public void update(TbContent content) {
		
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();//改签categoryId
		redisTemplate.boundHashOps("content").delete(categoryId);//清理缓存
		
		contentMapper.updateByPrimaryKey(content);
		
		
		//如果改了categoryId,则清理更改后的缓存
		if (categoryId.longValue()!=content.getCategoryId().longValue()) {
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
		
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id) {
		
		
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			
			//清理缓存
			redisTemplate.boundHashOps("content").delete(contentMapper.selectByPrimaryKey(id).getCategoryId());
			
			contentMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();

		if (content != null) {
			if (content.getTitle() != null && content.getTitle().length() > 0) {
				criteria.andTitleLike("%" + content.getTitle() + "%");
			}
			if (content.getUrl() != null && content.getUrl().length() > 0) {
				criteria.andUrlLike("%" + content.getUrl() + "%");
			}
			if (content.getPic() != null && content.getPic().length() > 0) {
				criteria.andPicLike("%" + content.getPic() + "%");
			}
			if (content.getStatus() != null && content.getStatus().length() > 0) {
				criteria.andStatusLike("%" + content.getStatus() + "%");
			}

		}

		Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	// 根据广告类型 ID 查询列表
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		// 先从缓存中读取
		List<TbContent> contentList= (List<TbContent>)redisTemplate.boundHashOps("content").get(categoryId);

		if (contentList == null) {//缓存中没有
			TbContentExample contentExample = new TbContentExample();
			Criteria criteria2 = contentExample.createCriteria();
			criteria2.andCategoryIdEqualTo(categoryId);
			criteria2.andStatusEqualTo("1");// 开启状态
			contentExample.setOrderByClause("sort_order");// 排序
			
			contentList = contentMapper.selectByExample(contentExample);//获取广告列表
			redisTemplate.boundHashOps("content").put(categoryId, contentList);//存入缓存
		} else {
			System.out.println("从缓存读取数据");
		}

		return contentList;
	}

}
