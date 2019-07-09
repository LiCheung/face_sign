package com.twelve;

import com.twelve.model.login.Member;
import com.twelve.repository.MemberRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class MemberService implements UserDetailsService {

	@Autowired
	MemberRepo memberRepo;

	@Override
	public UserDetails loadUserByUsername(String name) {

		Member member= memberRepo.findByName(name);
		if(member == null){
			throw new UsernameNotFoundException("用户名不存在");
		}
		return member;
	}

}
