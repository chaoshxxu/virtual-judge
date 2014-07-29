package judge.tool;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

public class MyFilter implements Filter{

	private SessionContext myc;
	
	private ForbiddenVisitorRuler forbiddenVisitorRuler;
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (forbiddenVisitorRuler == null) {
			forbiddenVisitorRuler = SpringBean.getBean("forbiddenVisitorRuler", ForbiddenVisitorRuler.class);
		}

		try {

			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			HttpSession session = request.getSession();

			String ua = request.getHeader("user-agent");

			// get X-Real-IP if the request comes from nginx
			String ip = request.getHeader("X-Forwarded-For");
			if (StringUtils.isEmpty(ip)) {
				ip = request.getHeader("X-Real-IP");
			}
			if (StringUtils.isEmpty(ip)) {
				ip = request.getRemoteAddr();
			}

			if (forbiddenVisitorRuler.forbidden(ua, ip)) {
				myc = SessionContext.getInstance();
				session.invalidate();
				myc.DelSession(session);
				
				response.sendRedirect("http://ip138.com/ips138.asp?ip=" + ip);
			} else {
				if (session.getAttribute("remoteAddr") == null) {
					session.setAttribute("remoteAddr", ip);
					session.setAttribute("user-agent", request.getHeader("user-agent"));
					session.setAttribute("referer", request.getHeader("referer"));
				}
				chain.doFilter(req, res);
			}
			
//			Enumeration paramNames = request.getParameterNames();
//			while (paramNames.hasMoreElements()) {
//				String paramName = (String) paramNames.nextElement();
//				System.out.print(paramName + "=");
//				String[] paramValues = request.getParameterValues(paramName);
//				for (String value : paramValues) {
//					System.out.print(value + " ");
//				}
//				System.out.println();
//			}
//			System.out.println("");

		} catch (Exception e) {
			e.printStackTrace();
		}

    }

	public void destroy() {
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}



}