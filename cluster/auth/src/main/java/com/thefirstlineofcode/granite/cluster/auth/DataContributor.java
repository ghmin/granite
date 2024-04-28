package com.thefirstlineofcode.granite.cluster.auth;

import com.thefirstlineofcode.granite.framework.adf.mybatis.DataContributorAdapter;
import org.pf4j.Extension;

@Extension
public class DataContributor extends DataContributorAdapter {

	@Override
	public Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_Account.class
		};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
				"AccountMapper.xml"
		};
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {
				"auth.sql"
		};
	}

}
