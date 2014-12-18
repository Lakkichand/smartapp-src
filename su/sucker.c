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
	// the superuser activity couldn't be started
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

//	char *exec_args[4];
//	exec_args[0] = "pm";
//	exec_args[1] = "disable-user";
//	exec_args[2] = "com.baidu.tieba";
//	exec_args[3] = NULL;
//	execv("/system/bin/pm", exec_args);
//	char *argvx[] = {"/system/bin/chmod", "uninstall", "com.baidu.tieba", NULL};
//	execv("chmod", argvx);
//	execv("/system/bin/ls", NULL);
	char *argvx[] = {"sh","system/bin/pm","disable-user","com.baidu.tieba",NULL};
	execvp("sh",argvx);
	return executionFailure("sh");
}

