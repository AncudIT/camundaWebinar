package de.ancud.camunda.simple.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.web.portlet.context.AbstractRefreshablePortletApplicationContext;
import org.springframework.web.servlet.ViewRendererServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

/**
 * Created by jan on 07.01.15.
 */
@WebListener
public class ServletInitializer extends AbstractRefreshablePortletApplicationContext implements ServletContextListener {
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanFactory);

        AnnotationBeanNameGenerator annotationBeanNameGenerator = new AnnotationBeanNameGenerator();
        reader.setBeanNameGenerator(annotationBeanNameGenerator);
        scanner.setBeanNameGenerator(annotationBeanNameGenerator);

        ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
        reader.setScopeMetadataResolver(scopeMetadataResolver);
        scanner.setScopeMetadataResolver(scopeMetadataResolver);

        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            for (String configLocation : configLocations) {
                try {
                    Class<?> clazz = getClassLoader().loadClass(configLocation);
                    if (logger.isInfoEnabled()) {
                        logger.info("Successfully resolved class for [" + configLocation + "]");
                    }
                    reader.register(clazz);
                }
                catch (ClassNotFoundException ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not load class for config location [" + configLocation +
                                "] - trying package scan. " + ex);
                    }
                    int count = scanner.scan(configLocation);
                    if (logger.isInfoEnabled()) {
                        if (count == 0) {
                            logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
                        }
                        else {
                            logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ViewRendererServlet viewRendererServlet = new ViewRendererServlet();
        ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", viewRendererServlet);
        if(dispatcherServlet != null) {
            dispatcherServlet.setLoadOnStartup(1);
            dispatcherServlet.addMapping("/WEB-INF/servlet/view");

            dispatcherServlet.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
            dispatcherServlet.setInitParameter("contextConfigLocation", "de.ancud.camunda.simple");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
