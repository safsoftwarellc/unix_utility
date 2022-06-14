package com.saf.unix;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("unixInfo")
public class UnixInfo {

	@Value("${unix.hostName}")
	private String hostName;
	
	@Value("${unix.userName}")
	private String userName;
	
	@Value("${unix.userPassCode}")
	private String userPassCode;
	
	@Value("${local.folder.path:/xyz/abc/uploadfiles}")
	private String localFolderPath;
	
	@Value("${remote.folder.path:/xyz/abc/dropfiles}")
	private String remoteFolderPath;
	
	
	public UnixInfo() {
		
	}
	
	public UnixInfo(String hostName, String userName, String userPassCode, String localFolderPath,
			String remoteFolderPath) {
		super();
		this.hostName = hostName;
		this.userName = userName;
		this.userPassCode = userPassCode;
		//this.localFolderPath = localFolderPath;
		//this.remoteFolderPath = remoteFolderPath;
	}
	
	public String getHostName() {
		return hostName;
	}
	public void setHostname(String hostname) {
		this.hostName = hostname;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassCode() {
		return userPassCode;
	}
	public void setUserPassCode(String userPassCode) {
		this.userPassCode = userPassCode;
	}
	public String getLocalFolderPath() {
		return localFolderPath;
	}
	public void setLocalFolderPath(String localFolderPath) {
		this.localFolderPath = localFolderPath;
	}
	public String getRemoteFolderPath() {
		return remoteFolderPath;
	}
	public void setRemoteFolderPath(String remoteFolderPath) {
		this.remoteFolderPath = remoteFolderPath;
	}
	
	
	
	
}
