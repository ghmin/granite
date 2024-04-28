package com.thefirstlineofcode.granite.cluster.auth;

import com.thefirstlineofcode.granite.framework.core.auth.Account;
import com.thefirstlineofcode.granite.framework.core.auth.IAuthenticator;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class Authenticator implements IAuthenticator {
	@Autowired
	private SqlSession sqlSession;

	@Override
	public Object getCredentials(Object principal) {
		AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
		
		Account account = mapper.selectByName((String)principal);
		if (account != null)
			return account.getPassword();
		
		return null;
	}

	@Override
	public boolean exists(Object principal) {
		AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
		int count = mapper.selectCountByName((String)principal);
		
		return count != 0;
	}

}
