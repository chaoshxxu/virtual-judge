package judge.remote.account;

import judge.remote.RemoteOj;

import org.apache.http.protocol.HttpContext;

public class RemoteAccount {
	
	private final RemoteOj remoteOj;
	private final String accountId;
	private final String password;
	private final String exclusiveCode;
	private final HttpContext context;
	
	public RemoteAccount(RemoteOj remoteOj, String accountId, String password, String exclusiveCode, HttpContext context) {
		super();
		this.remoteOj = remoteOj;
		this.accountId = accountId;
		this.password = password;
		this.exclusiveCode = exclusiveCode;
		this.context = context;
	}
	
	public RemoteOj getRemoteOj() {
		return remoteOj;
	}
	public String getAccountId() {
		return accountId;
	}
	public String getPassword() {
		return password;
	}
	public String getExclusiveCode() {
		return exclusiveCode;
	}
	public HttpContext getContext() {
		return context;
	}
	
}
