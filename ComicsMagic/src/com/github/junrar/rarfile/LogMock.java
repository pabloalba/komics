package com.github.junrar.rarfile;
public class LogMock {
private static LogMock rarLog = null;
	
	public static LogMock getLog(String name) {
		if (rarLog == null) {
			rarLog = new LogMock();
		}
		return rarLog;
	}
	
	
	
	public void info(String str){
		
	}
	
	public void debug(String str){
		
	}
	
	public void error(String str){
		
	}
}
