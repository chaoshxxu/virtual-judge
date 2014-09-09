package judge.tool;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import judge.service.JudgeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartUpListener implements ServletContextListener {
	private final static Logger log = LoggerFactory.getLogger(StartUpListener.class);

	/* 监听服务器启动 */
	public void contextInitialized(ServletContextEvent event) {
		log.info("系统启动");

		ServletContext sc = event.getServletContext();
		ApplicationContainer.sc = sc;

		Properties prop = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(sc.getRealPath("WEB-INF/classes/web.properties"));
			prop.load(in);
			for (Enumeration e = prop.propertyNames(); e.hasMoreElements(); ) {
				String key = (String)e.nextElement();
				sc.setAttribute(key, prop.getProperty(key).trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		sc.setAttribute("DataPath", "/data");
		sc.setAttribute("StandingDataPath", "/data/standing");
		sc.setAttribute("ContestSourceCodeZipFilePath", "/data/source");
		sc.setAttribute("version", new Date().getTime() + "");

		JudgeService judgeService = (JudgeService) SpringBean.getBean("judgeService");
//        judgeService.initJudge();
        judgeService.initProblemSpiding();
	}

	/* 监听服务器关闭 */
	public void contextDestroyed(ServletContextEvent event) {
		log.info("系统关闭");
	}
}
