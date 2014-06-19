/************************************************************************/
/* 
backup.c 备份源文件
命令: backup   [-d dataPath]  [-a apkPath]   BUSYBOX   DEST   PACKAGENAME
选项:-d 选择备份数据 dataPath 数据文件路径
功能:备份应用的apk以及数据。备份数据使用tar压缩备份，备份apk使用cp直接拷贝
*/
/************************************************************************/


#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include "define.h"

#define DBUG_FALG_BACKUP 0
#define DBUG_FLAG_BACKUP_APP 1

#define BACKUP_SUCCESS "backup success"  //备份成功
#define BACKUP_FAILURE "backup failure" //备份失败
#define BACKUP_APK_ERROR "backup app error" //备份apk失败
#define BACKUP_APK_SUCCESS "backup app success"  //备份apk成功
#define BACKUP_DATA_ERROR "backup data error"  //备份数据失败
#define BACKUP_DATA_SUCCESS "backup data success"  //备份数据成功

int backup_app_main(int argc, char** argv){
	char *cmd = NULL, *busybox = NULL, *apkPath = NULL, *dataPath = NULL, *descPath = NULL, *packageName = NULL;
	int c = 0, backup_data = 0, backup_apk = 0, arg_error = 0;
	int i = 0;

	struct option longopts[] = {
		{"data", 1, 0, 'd'},
		{"apk", 1, 0, 'a'},
		{ 0, 0, 0, 0}
	};

	opterr = 0;
	while((c = getopt_long(argc, argv, "d:a:", longopts, NULL)) != EOF){
		switch(c){
		case 'd':
			backup_data = 1;
			dataPath = optarg;
			DLOG_MSG(DBUG_FALG_BACKUP, dataPath);
			break;
		case 'a':
			backup_apk = 1;
			apkPath = optarg;
			DLOG_MSG(DBUG_FALG_BACKUP, apkPath);
			break;
		case '?':
			arg_error = 1;
			break;
		case ':':
			arg_error = 1;
			break;
		case -1:
			break;
		default:
			break;
		}
	};

	DLOG_INT(DBUG_FALG_BACKUP, arg_error);

	//命令需要4个操作数
	if(argc - optind != 3){
		DLOG_INT(DBUG_FALG_BACKUP, optind);
		DLOG_INT(DBUG_FALG_BACKUP, (argc-optind));
		arg_error = 1;
	}

	if(!arg_error){
		busybox = argv[optind];
		descPath = argv[optind+1];
		packageName = argv[optind+2];
		DLOG_MSG(DBUG_FALG_BACKUP, busybox);
		DLOG_MSG(DBUG_FALG_BACKUP, descPath);
		DLOG_MSG(DBUG_FALG_BACKUP, packageName);
	}

	if(arg_error == 1){
		printf("before exit : arg_error = %d\n", arg_error);
		fprintf(stderr, "%s\n%s\n", ERROR_ARGUMENT_INVALID, BACKUP_FAILURE);
		DLOG_INT(DBUG_FALG_BACKUP, arg_error);
		DLOG_MSG(DBUG_FALG_BACKUP, "backup arg_error!");
		return 1;
	}

	if(backup(busybox, apkPath, dataPath, descPath, packageName)){
		return 1;
	}
	return 0;
}

int backup(const char* busybox, const char* apkPath, const char* dataPath, const char* descPath, const char* packageName){
	char buffer[1024];
	int rc = 1;
	int endWithSeparator = 1;
	int backup_data = 0;
	int backup_apk = 0;

	if(apkPath != NULL){
		backup_apk = 1;
	}

	if(dataPath != NULL){
		backup_data = 1;
	}

	if((rc = strlen(descPath)) > 0){
		char c = descPath[rc -1];
		if(c != '/'){
			endWithSeparator = 0;
		}
	}
	
	if(backup_apk){
		//备份apk
		memset(buffer, 0, sizeof(buffer));
		strncat(buffer, descPath, sizeof(buffer) -1);
		if(!endWithSeparator){
			strncat(buffer, "/", sizeof(buffer) -1);
		}
		strncat(buffer, packageName, sizeof(buffer) -1);
		strncat(buffer, ".apk", sizeof(buffer)-1);
		if(backupApk(busybox, apkPath, buffer)){
			fprintf(stderr, "%s, %s\n", BACKUP_FAILURE, GO_FAILURE);
			return 1;
		}
	}

	//备份数据
	if(backup_data){
		memset(buffer, 0, sizeof(buffer));
		strncat(buffer, descPath, sizeof(buffer) -1);
		if(!endWithSeparator){
			strncat(buffer, "/", sizeof(buffer) -1);
		}
		strncat(buffer, packageName, sizeof(buffer) -1);
		strncat(buffer, ".tar.gz", sizeof(buffer) -1);
		if(backupData(busybox, dataPath, buffer)){
			fprintf(stderr, "%s, %s\n", BACKUP_FAILURE, GO_FAILURE);
			return 1;
		}
	}

	fprintf(stdout, "%s,%s\n", BACKUP_SUCCESS, GO_SUCCESS);
	return 0;
}

int backupApk(const char* busyboxPath, const char* apkPath, const char* descPath){
	char buffer[1024];
	char *errMsg = NULL;

	sprintf(buffer, "%s cp -f %s %s 1>%s 2>%s", busyboxPath, apkPath, descPath, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DBUG_FALG_BACKUP, buffer);
	if(DBUG_FLAG_BACKUP_APP) {
		fprintf(stdout, "%s\n", buffer);
	}
	system(buffer);
	if(DBUG_FLAG_BACKUP_APP) {
		errMsg = malloc(1024);
		getFile(errMsg, 1024, STDERR_LOG);
	}

	if(checkFileNotExitError() || !checkCmdResult()){
		if (errMsg != NULL) {
			fprintf(stderr, "%s\n", errMsg);
			free(errMsg);
			errMsg = NULL;
		}	
		fprintf(stderr, "%s\n", BACKUP_APK_ERROR);
		return 1;
	}
	fprintf(stdout, "%s\n", BACKUP_APK_SUCCESS);
	if (errMsg != NULL) {
		free(errMsg);
		errMsg = NULL;
	}	
	return 0;
}

int backupData(const char* busyboxPath, const char* srcPath, const char*desPath){
	char buffer[1024];
	char *errMsg = NULL;

	sprintf(buffer, "%s tar -zcf %s %s -C / --exclude lib* 1>%s 2>%s", busyboxPath, desPath, srcPath, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DBUG_FALG_BACKUP, buffer);
	if(DBUG_FLAG_BACKUP_APP) {
		fprintf(stdout, "%s\n", buffer);
	}
	system(buffer);

	if(DBUG_FLAG_BACKUP_APP) {
		errMsg = malloc(1024);
		getFile(errMsg, 1024, STDERR_LOG);
	}

	if(checkFileNotExitError() || !checkCmdResult()){
		if (errMsg != NULL) {
			fprintf(stderr, "%s\n", errMsg);
			free(errMsg);
			errMsg = NULL;
		}
		fprintf(stderr, "%s\n", BACKUP_DATA_ERROR);
		return 1;
	}
	fprintf(stdout, "%s\n", BACKUP_DATA_SUCCESS);
	if (errMsg != NULL) {
		free(errMsg);
		errMsg = NULL;
	}	
	return 0;
}
