package de.ancud.camunda.simple.config;

import com.liferay.portal.util.PortalUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocaleChangeInterceptor extends HandlerInterceptorAdapter {

	/**
	 * Default name of the locale specification parameter: "locale".
	 */
	public static final String DEFAULT_PARAM_NAME = "locale";

	private String paramName = DEFAULT_PARAM_NAME;


	/**
	 * Set the name of the parameter that contains a locale specification
	 * in a locale change request. Default is "locale".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Return the name of the parameter that contains a locale specification
	 * in a locale change request.
	 */
	public String getParamName() {
		return this.paramName;
	}


	@Override
	public boolean preHandle(PortletRequest request, PortletResponse response, Object handler){

		String newLocale = request.getParameter(this.paramName);
		if (newLocale != null) {
			LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
			if (localeResolver == null) {
				throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
			}
			HttpServletRequest httpRequest = PortalUtil.getHttpServletRequest(request);
			HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
			localeResolver.setLocale(httpRequest, httpResponse, StringUtils.parseLocaleString(newLocale));
		}
		// Proceed in any case.
		return true;
	}

}
