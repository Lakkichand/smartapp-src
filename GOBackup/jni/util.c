#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>
#include <pwd.h>
#include "define.h"

#define DBUG_FALG_UTIL 0

int checkStdout(const char* str){
	FILE* fp = NULL;
	int rc = 0;
	int result = 0;
	char buffer[4096];

	fp = fopen(STDOUT_LOG, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, sizeof(buffer) -1, fp);
		if(rc > 0){
			buffer[rc] = '\0';
			if(strstr(buffer, str) != NULL){
				result = 1;
			}
		}
		fclose(fp);
	}
	return result;
}

/* 
	return : 0 娌℃湁閿欒 1鎵惧埌閿欒
*/
int checkStderr(const char* error){
	FILE* fp = NULL;
	int rc = 0;
	int result = 0;
	char buffer[4096];

	fp = fopen(STDERR_LOG, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, sizeof(buffer) -1, fp);
		if(rc > 0){
			buffer[rc] = '\0';
			DLOG_MSG(DBUG_FALG_UTIL, buffer);
			if(strstr(buffer, error) != NULL){
				result = 1;
			}
		}
		fclose(fp);
	}
	return result;
}

/*
	检查是否发生No such file or directory错误
	return : 0 没有发生 1发生
*/
int checkFileNotExitError(){
	return checkStderr("No such file or directory");
}

/*
	检查命令执行结果是否正常
	return : 0 不正常 1 正常
*/
int checkCmdResult(){
	char buffer[1024];
	int rc = 0;
	int result = 0;
	FILE* fp = NULL;

	sprintf(buffer, "echo GOBackup_Cmd_result = $? 1>%s 2>%s", STDOUT_LOG, STDERR_LOG);
	rc = system(buffer);
	memset(buffer, 0, sizeof(buffer));

	fp = fopen(STDOUT_LOG, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, sizeof(buffer) -1, fp);
		if(rc > 0){
			buffer[rc] = '\0';
			DLOG_MSG(DBUG_FALG_UTIL, buffer);
			if(strstr(buffer, "GOBackup_Cmd_result = 0")){
				result = 1;
			}
		}
		fclose(fp);
	}
	return result;
}

#define SU_NORMAL "SU_NORMAL"
#define SU_ABNORMAL "SU_ABNORMAL"
int isSuNormal(){
	int myuid = -1;

	myuid = getuid();
	if(myuid != 0) {
		fprintf(stdout, "%s\n", SU_ABNORMAL);
		return 0;
	}
	fprintf(stdout, "%s\n", SU_NORMAL);
	return 0;
}

int isFileExist(const char *busybox, const char *filePath){
	char buffer[1024];

	sprintf(buffer, "%s ls -d %s 1>%s 2>%s", busybox, filePath, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DBUG_FALG_UTIL, buffer);
	system(buffer);
	if(checkFileNotExitError() || !checkCmdResult()){
		return 0;
	}
	return 1;
}

char *getParent(const char *file, char *parent){
	int length = 0;
	int curPos = 0;
	int c = 0;

	length = strlen(file);
	DLOG_INT(DBUG_FALG_UTIL, length);
	if(length == 0){
		return NULL;
	}

	if(length > 0 && (file[length - 1] == '\\' || file[length - 1] == '/')){
		length = length -1;
	}
	curPos = length - 1;
	
	c = file[curPos];
	while(c != '\\' && c != '/' && curPos >= 0){
		curPos--;
		c = file[curPos];
	}

	DLOG_INT(DBUG_FALG_UTIL, curPos);
	if(curPos < 0){
		return NULL;
	}

	strncpy(parent, file, curPos);
	DLOG_MSG(DBUG_FALG_UTIL, parent);
	return parent;
}

int getUidByUserName(const char* userName){
	struct passwd *user = NULL;
	user=getpwnam(userName);
	if(user == NULL){
		return 0;
	}
	return user->pw_uid;
}

/*
	获取指定文件内容
	return : 0 成功 1 失败
*/
int getFile(char *buffer, const int size, const char *file){
	FILE* fp = NULL;
	int rc = 0;
	int result = 0;

	if (buffer == NULL || file == NULL) {
		result = 1;
		return result;
	}

	memset(buffer, 0, size);
	fp = fopen(file, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, size -1, fp);
		if(rc > 0){
			buffer[rc] = '\0';
		}
		fclose(fp);
		result = 0;
	} else {
		result = 1;
	}
	return result;
}
