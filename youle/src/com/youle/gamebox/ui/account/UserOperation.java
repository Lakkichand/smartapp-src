package com.youle.gamebox.ui.account;

import com.youle.gamebox.ui.bean.LogAccount;

import java.util.List;


public interface UserOperation {
	public void saveAcount(LogAccount account);
	public void deleteAccount(String username);
	public List<LogAccount> getAccountList() ;
}
