package de.ancud.camunda.simple.config;

import com.liferay.portal.kernel.util.InfrastructureUtil;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.container.ManagedProcessEngineFactoryBean;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.portlet.multipart.CommonsPortletMultipartResolver;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.text.DateFormat;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA. User: jan Date: 29.07.13 Time: 15:15 To change
 * this template use File | Settings | File Templates.
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class PortletContext {
	private static final String PROPERTY_NAME_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	private static final String PROPERTY_NAME_HIBERNATE_HBMDDL = "hibernate.hbm2ddl.auto";
	private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";
	private static final String PROPERTY_NAME_MAX_UPLOAD_SIZE = "max.upload.size";
	@Autowired
	private ProcessEngine processEngine;

	@Resource
	private Environment env;

	@Resource
	private ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource;

	@Bean(name = "portletMultipartResolver")
	public CommonsPortletMultipartResolver portletMultipartResolver() {
		final CommonsPortletMultipartResolver commonsPortletMultipartResolver = new CommonsPortletMultipartResolver();
		commonsPortletMultipartResolver.setMaxUploadSize(Long.parseLong(env
				.getRequiredProperty(PROPERTY_NAME_MAX_UPLOAD_SIZE)));
		return commonsPortletMultipartResolver;
	}

	@Bean
	public DataSource dataSource() {
		return InfrastructureUtil.getDataSource();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean
				.setPersistenceProviderClass(HibernatePersistence.class);
		entityManagerFactoryBean
				.setPackagesToScan(env
						.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
		entityManagerFactoryBean.setJpaProperties(hibProperties());
		entityManagerFactoryBean.setPersistenceUnitName("default");

		return entityManagerFactoryBean;
	}

	private Properties hibProperties() {
		Properties properties = new Properties();
		properties.put(PROPERTY_NAME_HIBERNATE_SHOW_SQL,
				env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_SHOW_SQL));
		properties.put(PROPERTY_NAME_HIBERNATE_HBMDDL,
				env.getRequiredProperty(PROPERTY_NAME_HIBERNATE_HBMDDL));
		return properties;
	}

	@Bean
	public JpaTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory()
				.getObject());
		return transactionManager;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean
				.setValidationMessageSource(reloadableResourceBundleMessageSource);
		return localValidatorFactoryBean;
	}

	@Bean
	public FormattingConversionService conversionService() {

		// Use the DefaultFormattingConversionService but do not register
		// defaults
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(
				true);

		DateFormatter df = new DateFormatter();
		df.setStyle(DateFormat.SHORT);
		conversionService.addFormatter(df);

		return conversionService;
	}

	/* Camunda configuration */
	@Bean(name = "processEngine")
	public ManagedProcessEngineFactoryBean getProcessEngine() throws Exception {
		ManagedProcessEngineFactoryBean mpefb = new ManagedProcessEngineFactoryBean();
		mpefb.setProcessEngineConfiguration(getProcessEngineConfiguration());
		return mpefb;
	}

	private ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
		SpringProcessEngineConfiguration pec = new SpringProcessEngineConfiguration();
		pec.setProcessEngineName("engine");
		pec.setDataSource(dataSource());
		pec.setTransactionManager(transactionManager());
		pec.setDatabaseSchemaUpdate("true");
		pec.setJobExecutorActivate(true);
		return pec;
	}

	@Bean
	public RepositoryService getRepositoryService() throws Exception {
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService getRuntimeService() throws Exception {
		return processEngine.getRuntimeService();
	}

	@Bean
	public TaskService getTaskService() throws Exception {
		return processEngine.getTaskService();
	}

	@Bean
	public HistoryService getHistoryService() throws Exception {
		return processEngine.getHistoryService();
	}

	@Bean
	public ManagementService getManagementService() throws Exception {
		return processEngine.getManagementService();
	}

	@Bean
	public FormService getFormService() throws Exception {
		return processEngine.getFormService();
	}
}
