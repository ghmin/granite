package com.thefirstlineofcode.granite.cluster.auth;

import com.thefirstlineofcode.granite.framework.core.auth.Account;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@Component
public class AccountManager implements IAccountManager {
	
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public void add(Account account) {
		getMapper().insert(account);;
	}

	private AccountMapper getMapper() {
		return (AccountMapper)sqlSession.getMapper(AccountMapper.class);
	}

	@Override
	public void remove(String name) {
		getMapper().delete(name);
	}

	@Override
	public boolean exists(String name) {
		return getMapper().selectCountByName(name) != 0;
	}

	@Override
	public Account get(String name) {
		return getMapper().selectByName(name);
	}

	@Override
	public void add(String userName, String password) {
		D_Account account = new D_Account();
		account.setId(UUID.randomUUID().toString());
		account.setName(userName);
		account.setPassword(password);
		
		add(account);
	}

}
