package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

/*
 *
 * 认证类：实现注册和登录时时密码加密
 * 实现spring-security的UserDetailsService接口
 * 
 * */

public class UserDetailsServiceImpl implements UserDetailsService {
	
	
	private SellerService sellerService;

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	/*
	 * 
	 * */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("经过了UserDetailsServiceImpl");
		// 构建角色列表
		List<GrantedAuthority> grantAuths = new ArrayList();
		grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

		// 得到商家对象
		// ?参数:id	此处username就是sellerId
		TbSeller seller = sellerService.findOne(username);

		if (seller != null && seller.getStatus().equals("1")) {// 审核通过
				
			return new User(username, seller.getPassword(), grantAuths);
			
		} else {
			
			return null;
		}
	}

}
