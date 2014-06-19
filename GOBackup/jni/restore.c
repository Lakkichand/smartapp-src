/************************************************************************/
/* 
restore.c 备份源文件
命令: ra   [-i] [-l] [-a apk] [-d path] [-i path] [-D LD_LIBARY_PATH] BUSYBOX
选项：-d 恢复数据 path指定数据路径
		  -i 指定apk安装后的数据路径
		  -a 指定恢复apk path指定apk文件路径
		  -D 指定LD_LIBARY_PATH（形式为：LD_LIBARY_PATH=）
		  -l 指定已FORWARD_LOCK方式安装apk
功能: 恢复apk或者数据。指定恢复数据时，需要同时指定-d -i选项，如果是android4.0机器以及以上，需要指定-D选项, -l选项指定用FORWARD_LOCK方式安装apk
*/
/************************************************************************/

#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <getopt.h>
#include <pwd.h>
#include "define.h"

#define DEBUG_FALG_RESTORE 0
#define DBUG_FLAG_RESTORE_APP 1

#define RESTORE_APK_ERROR "restore apk error" //恢复apk失败
#define RESTORE_APK_SUCCESS "restore apk success" //恢复apk成功
#define RESTORE_DATA_ERROR "restore data error"  //恢复数据失败
#define RESTORE_DATA_SUCCESS "restore data success" //恢复数据成功
#define RESTORE_SUCCESS "restore success"  //恢复成功
#define RESTORE_FAILURE "restore failure" //恢复失败
#define UNRESTORE_DATA "not restore data" 
#define ERROR_RESTORE_GET_UUID "restore_get_uuid_error"

int restore_app_main(int argc, char** argv){
	int forward_locked = 0, specified_ld_library_path = 0, restore_apk=0, restore_data = 0, arg_error = 0;
	char buffer[1024];
	char* dataPath = NULL;
	char* internalFilePath = NULL;
	char* ld_library_path = NULL;
	char* busygoxPath = NULL;
	char* apkPath = NULL;
	int c;


	struct option longopts[] = {
		{"data", 1, 0, 'd'},
		{"innerpath", 1, 0, 'i'},
		{"librarypath", 1, 0, 'D'},
		{"apk", 1, 0, 'a'},
		{"lock", 0, 0, 'l'},
		{ 0, 0, 0, 0}
	};

	opterr = 0;
	while ((c = getopt_long (argc, argv, "d:i:D:a:l", longopts, NULL)) != EOF){
		switch(c){
		case 'd':
			restore_data = 1;
			dataPath = optarg;
			DLOG_MSG(DEBUG_FALG_RESTORE, dataPath);
			break;
		case 'i':
			internalFilePath = optarg;
			break;
		case 'D':
			specified_ld_library_path = 1;
			ld_library_path = optarg;
			DLOG_MSG(DEBUG_FALG_RESTORE, ld_library_path);
			break;
		case 'a':
			restore_apk = 1;
			apkPath = optarg;
			break;
		case 'l':
			forward_locked = 1;
			break;
		case ':':
			arg_error = 1;
			break;
		case '?':
			arg_error = 1;
			break;
		case -1:
			break;
		default:
			break;
		}
	}//end of while

	if(argc - optind != 1){
		arg_error = 1;
	}else{
		busygoxPath = argv[optind];
		DLOG_MSG(DEBUG_FALG_RESTORE, busygoxPath);
	}

	if(restore_data == 1 && (dataPath == NULL || internalFilePath == NULL)){
		arg_error = 1;
	}
	if(specified_ld_library_path == 1 && ld_library_path == NULL){
		arg_error = 1;
	}
	if(restore_apk && apkPath == NULL){
		arg_error = 1;
	}

	if(arg_error == 1){
		fprintf(stderr, "%s\n%s, %s\n", ERROR_ARGUMENT_INVALID, RESTORE_FAILURE, GO_FAILURE);
		return 1;
	}

// 	if(restore_apk && restore_data){
// 		if(restoreApkAndData(ld_library_path, apkPath, busygoxPath, dataPath, internalFilePath, forward_locked)){
// 			DLOG_MSG(DEBUG_FALG_RESTORE, "restore apk and data failed");
// 			return 1;
// 		}
// 		fprintf(stdout, "%s, %s\n", RESTORE_SUCCESS, GO_SUCCESS);
// 		return 0;
// 	}

	if(restore_apk){
		if(restoreApk(ld_library_path, apkPath, forward_locked)){
			DLOG_MSG(DEBUG_FALG_RESTORE, "restore apk failed");
			fprintf(stdout, "%s, %s\n", RESTORE_FAILURE, GO_FAILURE);
			return 1;
		}
	}

	if(restore_data){
		if(restoreData(busygoxPath, dataPath, internalFilePath)){
			DLOG_MSG(DEBUG_FALG_RESTORE, "restore data failed");
			fprintf(stdout, "%s, %s\n", RESTORE_FAILURE, GO_FAILURE);
			return 1;
		}
	}
	fprintf(stdout, "%s, %s\n", RESTORE_SUCCESS, GO_SUCCESS);
	return 0;
}

/*恢复
	lb_libary_path:linux系统的动态链接库路径(android 4.0以上机器使用pm需要指定这个路径)
*/
int restoreApkAndData(const char* lb_libary_path, const char* apkPath, const char* busyboxPath, const char* dataPath, const char* internalDataPath, int forwardLocked){
	char buffer[1024];
	int result = 0;

	//恢复apk
	if(restoreApk(lb_libary_path, apkPath, forwardLocked)){
		//安装apk失败
		return result;
	}

	//恢复数据
	if(restoreData(busyboxPath, dataPath, internalDataPath)){
		//恢复数据失败
		return result;
	}
	return result;
}

int restoreApk(const char* lb_libary_path, const char* apkPath, int forwardLocked){
	char buffer[1024];
	int result = 1;
	char* forwardLockedArg = NULL;
	char *errMsg = NULL;

	if(lb_libary_path != NULL){
		if(!strncmp(lb_libary_path, "null", 4)){
			lb_libary_path = NULL;
		}
	}
	DLOG_MSG(DEBUG_FALG_RESTORE, lb_libary_path);
	DLOG_MSG(DEBUG_FALG_RESTORE, apkPath);
	DLOG_INT(DEBUG_FALG_RESTORE, forwardLocked);
	if(forwardLocked == 1){
		forwardLockedArg = "-l ";
	}else{
		forwardLockedArg = "";
	}

	if(lb_libary_path != NULL){
		sprintf(buffer, "%s pm install -r %s%s 1>%s 2>%s", lb_libary_path, forwardLockedArg, apkPath, STDOUT_LOG, STDERR_LOG);
	}else{
		sprintf(buffer, "pm install -r %s%s 1>%s 2>%s", forwardLockedArg, apkPath, STDOUT_LOG, STDERR_LOG);
	}

	system(buffer);

	if (DBUG_FLAG_RESTORE_APP) {
		errMsg = malloc(1024);
		getFile(errMsg, 1024, STDERR_LOG);
	}
	if(!checkStderr("Failure") && checkStdout("Success")){
		fprintf(stdout, "%s\n", RESTORE_APK_SUCCESS);
		result = 0;
	}else{
		// 出错，打印日记信息
		fprintf(stderr, "%s\n", buffer);
		if (errMsg != NULL) {
			fprintf(stderr, "%s\n", errMsg);
		}
		fprintf(stderr, "%s\n", RESTORE_APK_ERROR);
		result = 1;
	}
	if (errMsg != NULL) {
		free(errMsg);
		errMsg = NULL;
	}
	return result;
}

int restoreData(const char* busyboxFilePath, const char * dataFileFullPath, const char* internalDataFilePath)
{
	int result = 0;
	char buffer[1024];
	int oldUid = 0;
	struct stat statBuffer;
	char *errMsg = NULL;

	//判断应用有没有安装，存不存在相对应的data目录
	if(!isFileExist(busyboxFilePath, internalDataFilePath)){
		if (DBUG_FLAG_RESTORE_APP) {
			fprintf(stderr, "%s is not exit!\n", internalDataFilePath);
		}
		fprintf(stderr, "%s\n%s\n", RESTORE_DATA_ERROR);
		return 1;
	}

	//查找应用data目录的用户id
	result = statDataDirectoryAttr(internalDataFilePath, &statBuffer);
	if(!result){
		oldUid = statBuffer.st_uid;
	}
	DLOG_INT(DEBUG_FALG_RESTORE, oldUid);
	if(oldUid == 0){//查找uid失败，返回
		fprintf(stderr, "%s\n", RESTORE_DATA_ERROR);
		result = 1;
		if (DBUG_FLAG_RESTORE_APP) {
			fprintf(stderr, " can not find olduid!\n");
		}
		return result;
	}

	//恢复数据
	memset(buffer, 0, sizeof(buffer));
	sprintf(buffer, "%s tar -zxf %s -C /  1>%s 2>%s \n", busyboxFilePath, dataFileFullPath, STDOUT_LOG, STDERR_LOG);
	DLOG_MSG(DEBUG_FALG_RESTORE, buffer);
	system(buffer);

	if(DBUG_FLAG_RESTORE_APP) {
		errMsg = malloc(1024);
		getFile(errMsg, 1024, STDERR_LOG);
	}

	if(checkFileNotExitError() || !checkCmdResult()){
		// 恢复数据出错，打印日记信息
		fprintf(stderr, "%s\n", buffer);
		if (errMsg != NULL) {
			fprintf(stderr, "%s\n", errMsg);
			free(errMsg);
			errMsg = NULL;
		}
		fprintf(stderr, "%s\n", RESTORE_DATA_ERROR);
		result = 1;
		return result;
	}

	memset(buffer, 0, sizeof(buffer));
	if(!changeDataDirectoryAttr(busyboxFilePath, internalDataFilePath, oldUid)){
		fprintf(stdout, "%s\n", RESTORE_DATA_SUCCESS);
		result = 0;
	}else{
		if (DBUG_FLAG_RESTORE_APP) {
			fprintf(stderr, "can not changeDataDIrectoryAttr!\n");	
		}
		fprintf(stderr, "%s\n", RESTORE_DATA_ERROR);
		result = 1;
	}
	if (errMsg != NULL) {
		free(errMsg);
		errMsg = NULL;
	}
	return result;
}

int statDataDirectoryAttr(const char* dataFilePath, struct stat* pStatBuffer)
{
	if(stat(dataFilePath, pStatBuffer) != 0){
		return 1;
	}else{
		return 0;
	}
}

int changeDataDirectoryAttr(const char* busyboxFilePath, const char* dataFilePath, int uid){
	int result = 0;
	char buffer[1024];
	char *errMsg = NULL;

	sprintf(buffer, "%s chown -R %d:%d %s 1>%s 2>%s", busyboxFilePath, uid, uid, dataFilePath, STDOUT_LOG, STDERR_LOG);
	system(buffer);
	if (DBUG_FLAG_RESTORE_APP) {
		errMsg = malloc(1024);
		getFile(errMsg, 1024, STDERR_LOG);
	}
	DLOG_MSG(DEBUG_FALG_RESTORE, buffer);
	if(checkStderr("Read-only file system") || !checkCmdResult()){
		//修改文件属性失败
		if (errMsg != NULL) {
			fprintf(stderr, "%s\n", errMsg);
			free(errMsg);
			errMsg = NULL;
		}
		fprintf(stderr, "%s\n", RESTORE_DATA_ERROR);
		result = 1;
		return result;
	}

	memset(buffer, 0, sizeof(buffer));
	int systemUid = getUidByUserName("system");
	DLOG_INT(DEBUG_FALG_RESTORE, systemUid);
	if(systemUid == 0){
		systemUid = 1000;
	}
	sprintf(buffer, "%s chown -R %d:%d %s/lib* 1>%s 2>%s", busyboxFilePath, systemUid, systemUid, dataFilePath, STDOUT_LOG, STDERR_LOG);
	system(buffer);
	if (errMsg != NULL) {
		free(errMsg);
		errMsg = NULL;
	}
	return result;
}
