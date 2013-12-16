package net.kaleidos.comicsmagic.helper;

public class RarLog {
	private static RarLog rarLog = null;
	
	public static RarLog getLog(String name) {
		if (rarLog == null) {
			rarLog = new RarLog();
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
