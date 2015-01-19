package de.ancud.camunda.simple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.portlet.HandlerMapping;
import org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.portlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;

/**
 * Created by jan on 07.01.15.
 */
@Configuration
public class RootContext {
    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource configureReloadableResourceBundleMessageSource()
    {
        final ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
        reloadableResourceBundleMessageSource.setBasename("classpath:messages");
        reloadableResourceBundleMessageSource.setDefaultEncoding("UTF-8");
        reloadableResourceBundleMessageSource.setCacheSeconds(10);
        return reloadableResourceBundleMessageSource;
    }

    @Bean(name = "viewResolver")
    public ViewResolver configureInternalResourceViewResolver()
    {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setCache(false);
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    @Bean
    public AnnotationMethodHandlerAdapter configureAnnotationMethodHandlerAdapter()
    {
        final AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter = new AnnotationMethodHandlerAdapter();
        annotationMethodHandlerAdapter.setWebBindingInitializer(new ConfigurableWebBindingInitializer());
        return annotationMethodHandlerAdapter;
    }

    @Bean
    public HandlerMapping configureHandlerMapping()
    {
        final DefaultAnnotationHandlerMapping defaultAnnotationHandlerMapping = new DefaultAnnotationHandlerMapping();
        defaultAnnotationHandlerMapping.setInterceptors(new Object[] { this.configureLocaleChangeInterceptor() });
        return defaultAnnotationHandlerMapping;
    }

    @Bean
    public LocaleChangeInterceptor configureLocaleChangeInterceptor()
    {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Bean
    public CookieLocaleResolver configureCookieLocaleResolver()
    {
        final CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(new Locale("en"));
        return cookieLocaleResolver;
    }
}
