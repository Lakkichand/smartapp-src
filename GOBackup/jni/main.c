#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "define.h"

int main(int argc, char** argv){
	char* cmd = NULL;

	if(argc <2){
		fprintf(stderr, "%s\n", ERROR_ARGUMENT_INVALID);
		return 1;
	}

	return do_main(argc-1, argv+1);
}

int do_main(int argc, char** argv){
	char* cmd = NULL;

	cmd = argv[0];
	if(!strcmp(cmd, CMD_BACKUP)){
		return backup_app_main(argc, argv);
	}

	if(!strcmp(cmd, CMD_RESTORE)){
		return restore_app_main(argc, argv);
	}

	if(!strcmp(cmd, CMD_CHECK_SU)){
		isSuNormal();
		return 0;
	}

	if(!strcmp(cmd, CMD_WIFI)){
		return wifi_main(argc, argv);
	}

	if(!strcmp(cmd, CMD_ACCOUNT)){
		return account_main(argc, argv);
	}

	fprintf(stderr, "%s\n", ERROR_ARGUMENT_INVALID);
	return 1;
}
