package com.thefirstlineofcode.granite.cluster.dba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import com.thefirstlineofcode.granite.framework.adf.mybatis.AdfSqlSessionFactoryBuilder;
import com.thefirstlineofcode.granite.framework.adf.mybatis.IDataContributor;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.pf4j.Extension;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.thefirstlineofcode.granite.framework.adf.core.ISpringConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.utils.IoUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Extension
@Configuration
public class DbaClusterConfiguration implements ISpringConfiguration,
		IServerConfigurationAware, IApplicationComponentServiceAware {
	private String configurationDir;

	private String dbName;

	// pg db begin
	private IApplicationComponentService appComponentService;


	static BasicDataSource realDbSource=null;
	@Bean
	public DataSource dataSource() {


		BasicDataSource dataSource = new BasicDataSource();
		// db config begin
		synchronized (this) {
			if (realDbSource != null)
				return realDbSource;

			File dbConfigFile = new File(configurationDir, "/db.ini");
			Properties properties = new Properties();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(dbConfigFile));
				properties.load(reader);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(String.format("DB configuration file %s not found.", dbConfigFile), e);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Can't read DB configuration file %s.", dbConfigFile), e);
			} finally {
				IoUtils.closeIO(reader);
			}


			String dbName = properties.getProperty("db.name");
			String userName = properties.getProperty("user.name");
			String password = properties.getProperty("password");
			String url = properties.getProperty("url");

			if (dbName == null) {
				throw new RuntimeException("Invalid DB configuration. DB name is null.");
			}

			if (userName == null) {
				throw new RuntimeException("Invalid DB configuration. User name is null.");
			}

			if (password == null) {
				throw new RuntimeException("Invalid DB configuration. Password is null.");
			}
			if (url == null || url.isEmpty() || !url.toLowerCase().contains("jdbc:")) {
				throw new RuntimeException("Invalid jdbc url ! pls check url parameter in db.ini.");
			}

			this.dbName = dbName;

			dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
			//- 	String url = "jdbc:postgresql://localhost:5432/test";
			dataSource.setUrl(url.trim());
			dataSource.setUsername(userName);
			dataSource.setPassword(password);

			try {
				dataSource.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException("Can't create data source.", e);
			}

			realDbSource = dataSource;
		}
		// db config end
		return dataSource;
	}

	@Bean
	public DataSourceTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();

		List<IDataContributor> dataContributors = appComponentService.getPluginManager().getExtensions(IDataContributor.class);
		for (IDataContributor dataContributor : dataContributors) {
			URL[] initScripts = dataContributor.getInitScripts();
			if (initScripts == null || initScripts.length == 0)
				continue;

			for (URL initScript : initScripts) {
				resourceDatabasePopulator.addScript(new UrlResource(initScript));
			}
		}
		resourceDatabasePopulator.setContinueOnError(true);

		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource);
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

		return dataSourceInitializer;
	}


	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		configurationDir = serverConfiguration.getConfigurationDir();
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	@DependsOn("dataSource")
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setSqlSessionFactoryBuilder(new AdfSqlSessionFactoryBuilder(appComponentService));

		String sConfigLocation = "META-INF/mybatis/configuration.xml";
		URL configLocationUrl = getClass().getClassLoader().getResource(sConfigLocation);
		if (configLocationUrl == null) {
			throw new RuntimeException(String.format("Can't read MyBatis configuration file. Config location: %s", sConfigLocation));
		}
		sqlSessionFactoryBean.setConfigLocation(new UrlResource(configLocationUrl));

		return createSqlSessionFactory(sqlSessionFactoryBean);

	}

	private SqlSessionFactory createSqlSessionFactory(SqlSessionFactoryBean sqlSessionFactoryBean) {
		SqlSessionFactory sessionFactory;
		try {
			sessionFactory = sqlSessionFactoryBean.getObject();
		} catch (Exception e) {
			throw new RuntimeException("Can't create SQL session factory.", e);
		}

		return sessionFactory;
	}
	// pg db end


}
