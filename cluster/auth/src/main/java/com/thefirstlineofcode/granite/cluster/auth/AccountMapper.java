package com.thefirstlineofcode.granite.cluster.auth;

import com.thefirstlineofcode.granite.framework.core.auth.Account;

public interface AccountMapper {
	void insert(Account account);
	void delete(String name);
	Account selectByName(String name);
	int selectCountByName(String name);
}
