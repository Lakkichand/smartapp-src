#ifndef DEFINE_H
#define DEFINE_H

#define STDERR_LOG "/data/data/com.jiubang.go.backup.ex/files/.error.log"
#define STDOUT_LOG "/data/data/com.jiubang.go.backup.ex/files/.normal.log"

#define ERROR_ARGUMENT_INVALID "argument invalid error"
#define GO_SUCCESS "GO_SUCCESS"
#define GO_FAILURE "GO_FAILURE"
#define CMD_BACKUP "ba"
#define CMD_RESTORE "ra"
#define CMD_CHECK_SU "cs"  //check su
#define CMD_WIFI  "bw"//wifi备份恢复命令
#define CMD_ACCOUNT "bc" //account备份恢复命令

#define DLOG_MSG(debug, msg) {if((debug)) printf(#msg " = %s\n", msg);}
#define DLOG_INT(debug, i) {if((debug)) printf(#i " = %d\n", i);}

#define DEBUG_FALG 0

extern int main(int argc, char **argv);
extern int do_main(int argc, char **argv);
extern int backup_app_main(int argc, char **argv);
extern int restore_app_main(int argc, char **argv);
extern int wifi_main(int argc, char **argv);
extern int account_main(int argc, char **argv);
extern int do_md5sum(int argc, char** argv);

extern int backup(const char *busybox, const char *apkPath, const char *dataPath, const char *descPath, const char *packageName);
extern int backupApk(const char *busyboxPath, const char *apkPath, const char *descPath);
extern int backupData(const char *busyboxPath, const char *srcPath, const char*desPath);

extern int restoreApkAndData(const char* lb_libary_path, const char* apkPath, const char* busyboxPath, const char* dataPath, const char* internalDataPath, int forwardLocked);
extern int restoreApk(const char* lb_libary_path, const char* apkPath, int forwardLocked);
extern int restoreData(const char* busyboxFilePath, const char * dataFileFullPath, const char* internalDataFilePath);
extern int statDataDirectoryAttr(const char *dataFilePath, struct stat *pStatBuffer);
extern int changeDataDirectoryAttr(const char *busyboxFilePath, const char *dataFilePath, int uid);

//wifif备份恢复
extern int backupWifi(const char *busybox, const char *path);
extern int restoreWifi(const char *busybox, const char *path, const char *desc);
extern char *findWifiFilePath(const char *busybox);

//帐号备份恢复
extern int backupAccount(const char* busybox, const char* desc);
extern int restoreAccount(const char* busybox, const char* src, const char* desc);

// 工具方法
extern int checkCmdResult();
extern int checkFileNotExitError();
extern int checkStderr(const char *error);
extern int checkStdout(const char *str);
extern int getUidByUserName(const char *userName);
extern int isSuNormal();
extern int isFileExist(const char *busybox, const char *filePath);
extern char *getParent(const char *file, char *parent);
extern int getFile(char *buffer, const int size, const char *file);

#endif
