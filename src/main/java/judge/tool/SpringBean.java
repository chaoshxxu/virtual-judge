package judge.tool;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringBean {

	public static Object getBean (String beanName, ServletContext sc) {
		WebApplicationContext wc = WebApplicationContextUtils.getWebApplicationContext(sc);
		return wc.getBean(beanName);
	}

	public static Object getBean (String beanName) {
		WebApplicationContext wc = WebApplicationContextUtils.getWebApplicationContext(ApplicationContainer.sc);
		return wc.getBean(beanName);
	}

	public static <T> T getBean (String beanName, Class T) {
		WebApplicationContext wc = WebApplicationContextUtils.getWebApplicationContext(ApplicationContainer.sc);
		return (T) wc.getBean(beanName);
	}
}
