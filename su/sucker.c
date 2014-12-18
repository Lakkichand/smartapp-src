#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include <errno.h>
#include <sys/stat.h>

#include <unistd.h>
#include <time.h>

#include <pwd.h>

static int g_puid;

static void printRow(int argc, char** argv, char** azColName)
{
	int i;
	for (i = 0; i < argc; i++)
	{
		printf("%s: %s\n", azColName[i], argv[i]);
	}
}

static int executionFailure(char *context)
{
	fprintf(stderr, "su: %s. Error:%s\n", context, strerror(errno));
	return -errno;
}

static int permissionDenied()
{
	printf("su: permission denied\n");
	return 1;
}

int main(int argc, char **argv)
{
	struct stat stats;
	struct passwd *pw;
	int uid = 0;
	int gid = 0;

	int ppid = getppid();
	char szppid[256];
	sprintf(szppid, "/proc/%d", ppid);
	stat(szppid, &stats);
	g_puid = stats.st_uid;

	if(setgid(gid) || setuid(uid)) 
		return permissionDenied();

	char sysCmd[1024];
	sprintf(sysCmd, "am start -a android.intent.action.MAIN -n com.zhidian.wifibox/com.zhidian.wifibox.activity.MainActivity --ei uid %d --ei pid %d > /dev/null", g_puid, ppid);

	char *exec_args[argc + 1];
	exec_args[argc] = NULL;
	exec_args[0] = "sh";
	int i;
	for (i = 1; i < argc; i++)
	{
		exec_args[i] = argv[i];
	}
	execv("/system/bin/sh", exec_args);
	return executionFailure("sh");
}
