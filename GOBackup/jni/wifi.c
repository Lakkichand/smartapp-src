/************************************************************************/
/* 
wifi.c源代码
命令: bw   [-b] [-r desc]  BUSYBOX PATH
选项: [-b] 备份wifi PATH指定备份路径
		[-r] 恢复wifi , desc指定系统恢复到, PATH指定源文件
功能: 备份恢复wifi设置， [-r]和[-b]不能同时指定
*/
/************************************************************************/

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/stat.h>
#include "define.h"

#define DBUG_FALG_WIFI 0

#define WIFI_BACKUP_ERROR "wfii backup error"
#define WIFI_BACKUP_SUCCESS "wifi backup success"
#define WIFI_RESTORE_ERROR "wifi restore error"
#define WIFI_RESTORE_SUCCESS "wifi restore success"
#define WIFI_SUCCESS "wifi success"
#define WIFI_FAILURE "wifi failure"
#define WIFI_FILE_NOT_FOUND "wifi file not found"
#define WIFI_PATH "WIFI_PATH"

#define WIFI_DEFAULT_PATH "/data/misc/wifi/wpa_supplicant.conf"
#define WIFI_OPTION_PATH1 "/data/wifi/bcm_supp.conf"
#define WIFI_OPTION_PATH2 "wpa.conf"
#define WIFI_OPTION_PATH3 "/data/misc/wifi/bcm_supp.conf"

int wifi_main(int argc, char **argv){
	int backup_wifi = 0, restore_wifi = 0;
	char *path = NULL;
	char *desc = NULL;
	char *busyboxFilePath = NULL;
	int c = 0;
	int arg_error = 0;

	struct option longopts[] = {
		{"backup", 0, 0, 'b'},
		{"restore", 1, 0, 'r'},
		{ 0, 0, 0, 0}
	};

	opterr = 0;
	while((c = getopt_long(argc, argv, "br:", longopts, NULL)) != EOF){
		switch(c){
		case 'b':
			backup_wifi = 1;
			break;
		case 'r':
			restore_wifi = 1;
			desc = optarg;
			DLOG_MSG(DBUG_FALG_WIFI, desc);
			break;
		case ':':
			arg_error = 1;
			break;
		case '?':
			arg_error = 1;
			break;
		default:
			break;
		}
	};

	if(argc - optind != 2){
		DLOG_INT(DBUG_FALG_WIFI, argc);
		DLOG_INT(DBUG_FALG_WIFI, optind);
		arg_error = 1;
	}

	DLOG_INT(DBUG_FALG_WIFI, arg_error);

	if(arg_error == 1){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, WIFI_FAILURE, GO_FAILURE);
		return 1;
	}

	busyboxFilePath = argv[optind];
	path = argv[optind+1];
	DLOG_MSG(DBUG_FALG_WIFI, busyboxFilePath);
	DLOG_MSG(DBUG_FALG_WIFI, path);

	if(backup_wifi == 1 && restore_wifi == 1){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, WIFI_FAILURE, GO_FAILURE);
		return 1;
	}

	if(backup_wifi == 0 && restore_wifi == 0){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, WIFI_FAILURE, GO_FAILURE);
		return 1;
	}

	if(path == NULL || busyboxFilePath == NULL){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, WIFI_FAILURE, GO_FAILURE);
		return 1;
	}

	if(restore_wifi == 1 && desc == NULL){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, WIFI_FAILURE, GO_FAILURE);
		return 1;
	}

	if(backup_wifi){
		if(backupWifi(busyboxFilePath, path)){
			return 1;
		}
	}

	if(restore_wifi){
		if(restoreWifi(busyboxFilePath, path, desc)){
			return 1;
		}
	}
	return 0;
}

int backupWifi(const char *busybox, const char *path){
	char buffer[1024];
	char *wifiFileFullPath = NULL;
	int find = 0;

	//查找wifi备份文件
	wifiFileFullPath = findWifiFilePath(busybox);
	if(wifiFileFullPath == NULL){
		//没有找到默认的wifi文件，备份失败
		DLOG_MSG(DBUG_FALG_WIFI, "can not find wifi file");
		fprintf(stderr, "%s\n%s, %s\n", WIFI_FILE_NOT_FOUND, WIFI_BACKUP_ERROR, GO_FAILURE);
		return 1;
	}

	//cp wifi备份文件
	sprintf(buffer, "%s cp -f %s %s 1>%s 2>%s", busybox, wifiFileFullPath, path, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DBUG_FALG_WIFI, buffer);
	system(buffer);

	if(checkFileNotExitError() || !checkCmdResult()){
		fprintf(stderr, "%s, %s\n", WIFI_BACKUP_ERROR, GO_FAILURE);
		return 1;
	}
	fprintf(stdout, "%s=%s\n%s, %s\n", WIFI_PATH, wifiFileFullPath, WIFI_BACKUP_SUCCESS, GO_SUCCESS);
	return 0;
}

int restoreWifi(const char *busybox, const char *path, const char *desc){
	char buffer[1024];
	int exist = 0;
	char fileNameBuf[256];
	char *parent = NULL;
	int wifiUid = 0;
	int wifiGid = 0;
	struct stat statBuffer;

 	if(isFileExist(busybox, desc)){
 		exist = 1;
 	}else{
		DLOG_MSG(DBUG_FALG_WIFI, "desc FILE NOT EIXT");
	}

	if(!exist){
		parent = getParent(desc, fileNameBuf);
		if(parent != NULL){
			DLOG_MSG(DBUG_FALG_WIFI, parent);
			exist = 1;
		}else{
			DLOG_MSG(DBUG_FALG_WIFI, "des get parent == null");
		}
	}

	if(!exist){
		fprintf(stderr, "%s\n%s\n%s\n", WIFI_FILE_NOT_FOUND, WIFI_RESTORE_ERROR, GO_FAILURE);
		return 1;
	}

	sprintf(buffer, "%s cp -f %s %s 1>%s 2>%s", busybox, path, desc, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DBUG_FALG_WIFI, buffer);
	system(buffer);

	if(checkFileNotExitError() || !checkCmdResult()){
		fprintf(stderr, "%s, %s\n", WIFI_RESTORE_ERROR, GO_FAILURE);
		return 1;
	}

	//修改文件属性为wifi
	if(!statDataDirectoryAttr(desc, &statBuffer)){
		wifiUid = statBuffer.st_uid;
		wifiGid = statBuffer.st_gid;
	}
	DLOG_INT(DBUG_FALG_WIFI, wifiUid);
	DLOG_INT(DBUG_FALG_WIFI, wifiGid);
	if(wifiUid == 0){
		wifiUid = 1010;
	}
	if(wifiGid == 0){
		wifiGid = 1010;
	}
	memset(buffer, 0, sizeof(buffer));
	sprintf(buffer, "%s chown -R %d:%d %s 1>%s 2>%s", busybox, wifiGid, wifiUid, desc, STDOUT_LOG, STDERR_LOG);
	system(buffer);
	if(checkFileNotExitError() || !checkCmdResult()){
		fprintf(stderr, "%s, %s\n", WIFI_RESTORE_ERROR, GO_FAILURE);
		return 1;
	}

	//改变文件的执行权限
	memset(buffer, 0, sizeof(buffer));
	sprintf(buffer, "%s chmod 660 %s 1>%s 2>%s", busybox, desc, STDOUT_LOG, STDERR_LOG);
	system(buffer);
	if(checkFileNotExitError() || !checkCmdResult()){
		fprintf(stderr, "%s, %s\n", WIFI_RESTORE_ERROR, GO_FAILURE);
		return 1;
	}

	fprintf(stdout, "%s, %s\n", WIFI_RESTORE_SUCCESS, GO_SUCCESS);
	return 0;
}

char *findWifiFilePath(const char *busybox){
#ifndef MAX_PATH
#define  MAX_PATH 4
#endif

	char *wifiFilePath[MAX_PATH] = {WIFI_DEFAULT_PATH, WIFI_OPTION_PATH1, WIFI_OPTION_PATH2, WIFI_OPTION_PATH3};
	int pos = 0;
	int find = 0;
	char *result = NULL;

	while(find == 0 && pos < MAX_PATH){
		if(isFileExist(busybox, wifiFilePath[pos])){
			find = 1;
			result = wifiFilePath[pos];
			break;
		}
		pos++;
	}
	return result;
}