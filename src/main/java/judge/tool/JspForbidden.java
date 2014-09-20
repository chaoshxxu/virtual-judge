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

public class JspForbidden implements Filter {
    /* 禁止用户直接访问JSP页面 */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        /* 获取客户端请求的上下文根 */
        String path = request.getContextPath();
        // log.info(path);

        /* 获取客户端请求的url */
        String url = request.getRequestURI();
        // log.info(url);

        if (url.endsWith("jsp")) {
            response.sendRedirect(path + "/toIndex.action");
        } else {
            chain.doFilter(req, res);
        }
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }
}