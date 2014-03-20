package judge.tool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Usage example:
 * 
 * client.getParams().setParameter("socks.host", "127.0.0.1");
 * client.getParams().setParameter("socks.port", 1081);
 * client.getConnectionManager().getSchemeRegistry().register(new Scheme("http", 80, new SocksSchemeSocketFactory()));
 * 
 * @author Isun
 *
 */
public class SocksSchemeSocketFactory implements SchemeSocketFactory {

	public Socket createSocket(final HttpParams params) throws IOException {
		if (params == null) {
			throw new IllegalArgumentException("HTTP parameters may not be null");
		}
		String proxyHost = (String) params.getParameter("socks.host");
		Integer proxyPort = (Integer) params.getParameter("socks.port");

		InetSocketAddress socksaddr = new InetSocketAddress(proxyHost, proxyPort);
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
		return new Socket(proxy);
	}

	public Socket connectSocket(final Socket socket, final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (remoteAddress == null) {
			throw new IllegalArgumentException("Remote address may not be null");
		}
		if (params == null) {
			throw new IllegalArgumentException("HTTP parameters may not be null");
		}
		Socket sock;
		if (socket != null) {
			sock = socket;
		} else {
			sock = createSocket(params);
		}
		if (localAddress != null) {
			sock.setReuseAddress(HttpConnectionParams.getSoReuseaddr(params));
			sock.bind(localAddress);
		}
		int timeout = HttpConnectionParams.getConnectionTimeout(params);
		try {
			sock.connect(remoteAddress, timeout);
		} catch (SocketTimeoutException ex) {
			throw new ConnectTimeoutException("Connect to " + remoteAddress.getHostName() + "/" + remoteAddress.getAddress() + " timed out");
		}
		return sock;
	}

	public boolean isSecure(final Socket sock) throws IllegalArgumentException {
		return false;
	}

}