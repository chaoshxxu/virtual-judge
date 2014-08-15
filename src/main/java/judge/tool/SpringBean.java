package judge.tool;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringBean {

	public static Object getBean (String beanName) {
		return getApplicationContext().getBean(beanName);
	}

	public static <T> T getBean (Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	public static <T> T getBean (String beanName, Class<T> clazz) {
		return getApplicationContext().getBean(beanName, clazz);
	}
	
	public static WebApplicationContext getApplicationContext() {
		return WebApplicationContextUtils.getWebApplicationContext(ApplicationContainer.sc);
	}
	
}
