package com.saf.unix;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import com.sshtools.net.SocketTransport;
import com.sshtools.publickey.ConsoleKnownHostsKeyVerification;
import com.sshtools.ssh.PasswordAuthentication;
import com.sshtools.ssh.SshAuthentication;
import com.sshtools.ssh.SshClient;
import com.sshtools.ssh.SshConnector;
import com.sshtools.ssh.SshException;
import com.sshtools.ssh2.Ssh2Client;
import com.sshtools.ssh2.Ssh2Context;

@Service
public class UnixService {
	
	@Autowired
	public UnixInfo unixInfo;
	
	private Logger LOGGER = LoggerFactory.getLogger(UnixService.class);
	
	Ssh2Client ssh = null;
	
	public boolean startUnixServerConnection(String rsaToken) {
		if(ssh==null) {
			int port = 22;
			try {
				SshConnector con = SshConnector.createInstance();
				con.getContext().setHostKeyVerification(new ConsoleKnownHostsKeyVerification());
				con.getContext().setPreferredPublicKey(Ssh2Context.PUBLIC_KEY_SSHDSS);
				SocketTransport t = new SocketTransport(unixInfo.getHostName(), port);
				t.setTcpNoDelay(true);
				
				SshClient ssh1 = con.connect(t, unixInfo.getUserName(), true);
				
				ssh = (Ssh2Client) ssh1;
				
				PasswordAuthentication pwd = new PasswordAuthentication();
				pwd.setPassword(unixInfo.getUserPassCode()+rsaToken);
				
				if(ssh.authenticate(pwd) != SshAuthentication.COMPLETE && ssh.isConnected()) {
					LOGGER.error("Unix Conection not completed!");
					return false;
				}else {
					LOGGER.info("Unix Conection completed!");
				}
				
				
			} catch (SshException | IOException e) {
				e.printStackTrace();
			}
			
		}
		if(ssh!=null)
			return ssh.isConnected();
		
		LOGGER.error("Unix Conection not created!");
		return false;
	}
	
	public void stopAndCloseUnixServerConnection() {
		if(ssh!=null && ssh.isConnected()) {
			ssh.disconnect();
			ssh.exit();
		}
		ssh = null;
	}
	
	
	public void copyLocalFileToUnixServer(String fileName) {
		File srcFile = new File(unixInfo.getLocalFolderPath() + fileName);
		File descFile = new File(unixInfo.getRemoteFolderPath() + fileName);
		try {
			FileUtils.copyFile(srcFile, descFile);
			LOGGER.info("Copied file from Source to Destination "+fileName);
		} catch (IOException e) {
			LOGGER.info("Unable to Copy file from Source to Destination "+fileName);
			e.printStackTrace();
		}
		
	}
	
	public void removeFileFromUnixServer(String fileName) {
		File delFile = new File(unixInfo.getRemoteFolderPath() + fileName);
		delFile.delete();
		LOGGER.info("Deleted the file from Server "+fileName);
		
	}
	
	
	public void placeFileInUnixLocation(String destUnixFolder) {
		File remoteFile = new File(unixInfo.getRemoteFolderPath());
		File dest = new File(destUnixFolder);
		try {
			FileSystemUtils.copyRecursively(remoteFile, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void runUnixJob(String destUnixFolder, String unixCommand) {
		executeCmd(destUnixFolder, unixCommand);
	}
	
	public List<String> getCommandOutputAsString(String destUnixFolder, String fileName){
		try {
			Thread.sleep(20000);
			List<String> listOfFileNames = new ArrayList<String>();
			File dir = new File(destUnixFolder);
			List<File> files = Arrays.asList(dir.listFiles());
			List<File> files1 = files.stream().filter(file -> file.getName().contains(fileName)).collect(Collectors.toList());
			for(File file: files1) {
				listOfFileNames.add(file.getName());
			}
			return listOfFileNames;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void removeFileFromUnixServer(String destUnixFolder, String fileName) {
		executeCmd(destUnixFolder, "rm -rf *"+fileName+"*");
	}
	
	
	
	public void executeCmd(String destUnixFolder, String unixCommand) {
		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", unixCommand);
		processBuilder.directory(new File(destUnixFolder));
		processBuilder.redirectErrorStream(true);
		
		try {
			Process process = processBuilder.start();
			boolean isProcssesExited= process.waitFor(15, TimeUnit.SECONDS);
			if(isProcssesExited) {
				List<String> results = readOutput(process.getInputStream());
				LOGGER.info("Sub process is terminatted successfully with a Value "+ process.exitValue()+" "+ results);
			}else {
				LOGGER.info("Sub process is running in the back ground!");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	private List<String> readOutput(InputStream inputStream) throws IOException{
		try(BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))){
			return output.lines().collect(Collectors.toList());
		}
	}
	
	public List<String> executeCommandForResults(String destUnixFolder, String unixCommand){
		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", unixCommand);
		if(unixCommand!=null && !unixCommand.isEmpty())
			processBuilder.directory(new File(destUnixFolder));
		processBuilder.redirectErrorStream(true);
		
		try {
			Process process = processBuilder.start();
			boolean isProcssesExited= process.waitFor(15, TimeUnit.SECONDS);
			if(isProcssesExited) {
				List<String> results = readOutput(process.getInputStream());
				LOGGER.info("Sub process is terminatted successfully with a Value "+ process.exitValue()+" "+ results);
				return results;
			}else {
				LOGGER.info("Sub process is running in the back ground!");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public String grepData(String filePath, String searchStringParam) {
		
		String[] searchStrings = searchStringParam.split("~");
		StringBuffer results = new StringBuffer();
		for(String searchString: searchStrings) {
			try {
				String command = String.format("grep -i \"%s\" %s", searchString, filePath);
				LOGGER.info("Command for exuection: {}", command);
				
				ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
				processBuilder.redirectErrorStream(true);
				Process process = processBuilder.start();
				boolean isProcssesExited= process.waitFor(15, TimeUnit.SECONDS);
				LOGGER.info("Sub process is terminatted with status ", command);
				if(isProcssesExited) {
					InputStream in = process.getInputStream();
					List<String> resultsList = readOutput(in);
					LOGGER.info("Results: ", resultsList);
					if(results.length()>0)
						results.append("~");
					results.append(String.join("~", resultsList));
				}

			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results.toString();
	}
	
	
	
}
