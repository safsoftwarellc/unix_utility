package com.saf.unix.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saf.unix.UnixInfo;
import com.saf.unix.UnixService;

@RestController
public class UnixRestController {
	
	@Autowired
	private UnixService unixService;
	
	@Autowired
	private Environment environment;

	
	@RequestMapping(path = "/rest/unix/connect", method = RequestMethod.POST)
	public ResponseEntity<Object> connectToUnix(@RequestParam("rsaToken") String rsaToken) {
		boolean isConnected = unixService.startUnixServerConnection(rsaToken);
		
		ResponseEntity<Object> response = null;
		if(isConnected) {
			new ResponseEntity<Object>("Connection Successful", HttpStatus.OK);
		}else {
			new ResponseEntity<Object>("Connection Failed", HttpStatus.UNAUTHORIZED);
		}
		return response;
	}
	
	@RequestMapping(path = "/rest/unix/connectWithData", method = RequestMethod.POST)
	public ResponseEntity<Object> connectToUnixWithData(@RequestBody UnixInfo unixInfo, @RequestParam("rsaToken") String rsaToken) {
		unixService.unixInfo = unixInfo;
		boolean isConnected = unixService.startUnixServerConnection(rsaToken);
		
		ResponseEntity<Object> response = null;
		if(isConnected) {
			new ResponseEntity<Object>("Connection Successful", HttpStatus.OK);
		}else {
			new ResponseEntity<Object>("Connection Failed", HttpStatus.UNAUTHORIZED);
		}
		return response;
	}
	
	@RequestMapping(path = "/rest/unix/disconnect", method = RequestMethod.GET)
	public ResponseEntity<Object> disconnectFromUnix() {
		unixService.stopAndCloseUnixServerConnection();
		return new ResponseEntity<Object>("Disconnect Successful", HttpStatus.OK);
	}
	
	@RequestMapping(path = "/rest/unix/placeFile", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> postFileToUnixServer(@RequestParam("unixPath") String unixPath,
			@RequestParam("unixUserCommand") String unixUserCommand, 
			@RequestParam("file") MultipartFile file) {
		
		String fileName = file.getOriginalFilename();
		String localFolderPath = unixService.unixInfo.getLocalFolderPath();
		String fileData = null;
		
		try {
			fileData = new String(file.getBytes());
			File file1 = new File(localFolderPath+"/"+file.getOriginalFilename());
			file1.createNewFile();
			FileWriter fileWriter=new FileWriter(file1);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.write(fileData);
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		unixService.copyLocalFileToUnixServer(fileName);
		unixService.placeFileInUnixLocation(unixPath);
		unixService.removeFileFromUnixServer(fileName);
		
		ResponseEntity<Object> response = new ResponseEntity<Object>("Posted Successful", HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(path = "/rest/unix/runJob", method = RequestMethod.POST)
	public ResponseEntity<Object> runUnixJob(@RequestParam("unixFolder") String unixFolder,
			@RequestParam("touchFileName") String touchFileName) {
		
		int dot = touchFileName.lastIndexOf(".");
		if(touchFileName.substring(dot+1).equalsIgnoreCase("sh")) {
			unixService.runUnixJob(unixFolder, touchFileName);
		}else {
			unixService.runUnixJob(unixFolder, "touch " + touchFileName);
		}
		return new ResponseEntity<Object>("Job run Successful", HttpStatus.OK);
	}
	
	@RequestMapping(path = "/rest/unix/getOutputForCommand", method = RequestMethod.POST)
	public ResponseEntity<List<String>> getOutputForCommand(@RequestParam("unixFolder") String unixFolder,
			@RequestParam("fileName") String fileName) {
		
		List<String> data = unixService.getCommandOutputAsString(unixFolder, fileName);
		return new ResponseEntity<List<String>>(data, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/rest/unix/removeFile", method = RequestMethod.POST)
	public ResponseEntity<Object> removeFileFromUnixServer(@RequestParam("unixFolder") String unixFolder,
			@RequestParam("fileName") String fileName) {
		unixService.removeFileFromUnixServer(unixFolder, fileName);
		return new ResponseEntity<Object>("File Removed Successful", HttpStatus.OK);
	}
	
	@RequestMapping(path = "/rest/unix/grepData")
	public ResponseEntity<Object> grepData(@RequestParam("filePath") String filePath, @RequestParam("searchString") String searchString) {
		String results = unixService.grepData(filePath, searchString);
		return new ResponseEntity<Object>(results, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/rest/unix/getOutputForCommandAsString")
	public ResponseEntity<Object> getOutputForCommandAsString(@RequestParam("filePath") String filePath, @RequestParam("command") String command) {
		List<String> results = unixService.executeCommandForResults(filePath, command);
		String resultsString = String.join("~", results);
		return new ResponseEntity<Object>(resultsString, HttpStatus.OK);
	}

	
	@RequestMapping(path = "/rest/unix/download", produces = "application/octet-stream")
	public ResponseEntity<Object> downloadFileFromUnixServer(@RequestParam("path") String pathString, 
			@RequestParam("fileName") String fileName) {
		System.out.println("Path Key = " + pathString +", fileName="+fileName);
		String path = environment.getProperty(pathString);
		File fileDownload = new File(path, fileName);
		System.out.println(fileDownload.getAbsolutePath());
		try {
			InputStreamResource resource = new InputStreamResource(new FileInputStream(fileDownload));
			return ResponseEntity.ok().contentLength(fileDownload.length())
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Object>("Unkown Issue", HttpStatus.NOT_FOUND);
	}


	
}
