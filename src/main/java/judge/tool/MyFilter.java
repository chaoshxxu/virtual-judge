package judge.tool;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyFilter implements Filter{

	private SessionContext myc;

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		try {

			HttpServletRequest request = (HttpServletRequest) req;
			HttpSession session = request.getSession();

			String ua = request.getHeader("user-agent");
			String ip = request.getRemoteAddr();

			// get X-Real-IP if the request comes from nginx
			String ip2 = request.getHeader("X-Real-IP");
			if (ip2 != null) ip = ip2;


			if (!legalUA(ua) || !legalIP(ip)) {
				myc = SessionContext.getInstance();
				session.invalidate();
				myc.DelSession(session);
			} else if (session.getAttribute("remoteAddr") == null) {
				session.setAttribute("remoteAddr", ip);
				session.setAttribute("user-agent", request.getHeader("user-agent"));
				session.setAttribute("referer", request.getHeader("referer"));
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

		chain.doFilter(req, res);
    }

	private boolean legalUA(String ua) {
		if (ua == null){
			return false;
		}
		ua = ua.toLowerCase();
		if (ua.isEmpty() || ua.contains("bot") || ua.contains("spider")){
			return false;
		}
		return true;
	}

	private boolean legalIP(String ip) {
		if (ip == null || ip.isEmpty()){
			return false;
		}
		return true;
	}



	public void destroy() {
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}



}