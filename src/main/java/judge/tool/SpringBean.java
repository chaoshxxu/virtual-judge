package judge.tool;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringBean {

	/* 获取spring中bean的实例 */
	public static Object getBean (String beanName, ServletContext sc) {
		WebApplicationContext wc = WebApplicationContextUtils.getWebApplicationContext(sc);
		return wc.getBean(beanName);
	}
}
