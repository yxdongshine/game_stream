package com.game.Mail;

import com.alibaba.fastjson.JSONObject;

public final class MailAccount {

	private String account;
	private String password;
	private String nickname;
	private String smtpHost;
	private String smtpPort;

	public MailAccount() {
		super();
	}

	public MailAccount(String account, String password, String smtpHost, String smtpPort) {
		super();
		this.account = account;
		this.password = password;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
	}

	public MailAccount(String account, String password, String nickname, String smtpHost, String smtpPort) {
		super();
		this.account = account;
		this.password = password;
		this.nickname = nickname;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}