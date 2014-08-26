package com.youle.gamebox.ui.bean;


import com.youle.gamebox.ui.util.CoderString;

public class LogAccount {
	private String userName ;
	private String password ;
	private String option ;
	private long lastLogin ;
	public static String AUTO_LOGIN="1" ;
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public long getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEncodePassword(){
		try {
			return CoderString.encrypt(password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "" ;
	}

	public String setDecryptPassword(String password){
		try {
			return CoderString.decrypt(password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "" ;
	}





}
