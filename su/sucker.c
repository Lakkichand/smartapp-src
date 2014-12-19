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

static int executionFailure(char *context) {
	fprintf(stderr, "su: %s. Error:%s\n", context, strerror(errno));
	return -errno;
}

int main(int argc, char **argv) {
	struct stat stats;
	struct passwd *pw;
	int uid = 0;
	int gid = 0;

	int ppid = getppid();
	char szppid[256];
	sprintf(szppid, "/proc/%d", ppid);
	stat(szppid, &stats);
	g_puid = stats.st_uid;

	if (setgid(gid) || setuid(uid)) {
		printf("get root permission fail!\n");
		return 1;
	}

	printf("get root permission success!\n");

	char cmd_t[2048] = { '\0', };
	int i;
	for (i = 1; i < argc; i++) {
		strcat(cmd_t, argv[i]);
		strcat(cmd_t, " ");
	}
	printf("%s\n",cmd_t);
	char *argvx[] = { "su", "-c", cmd_t, NULL };
	execvp("su", argvx);
	return executionFailure("sh");
}

