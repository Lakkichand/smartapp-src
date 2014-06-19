#include <string.h>
#include <stdio.h>
#include <conio.h>
#include <stdlib.h>
#include <errno.h> 
#include <sys/types.h>
#include <netinet.h>
#include <arpa/inet.h>
#include <resolv.h>
#include <sys/socket.h> 

#define PORT 1040
#define MAX_CONNECT 5


int main(int argc, char **argv){
	int root = 0;
	
	if(change_to_su()){
		return 1;
	}

	create_service_socket();
	exit(0);
}

int change_to_su(){
	int myuid = -1;
	char buffer[128];
	
	sprintf(buffer, "su");
	system(buffer);

	myuid = getuid();
	if(myuid != 0){
		return 1;
	}
	return 0;
}

int create_service_socket(){
	int listenfd, connfd, newfd;
	struct sockaddr_in servaddr;
	struct sockaddr_in inaddr;
	char buffer[2048];
	int rc;
	int done;
	int receiveLen;

	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.S_un.S_addr = htonl(INADDR_ANY);
	servaddr.sin_port = htons(PORT);
	rc = bind(listenfd, (struct sockaddr *)&servaddr), sizeof(servaddr));
	if(rc != 0){
		printf("after bind, rc = [%d] \n", rc);
		return rc;
	}
	rc = listen(listenfd, MAX_CONNECT);
	if(rc == -1){
		printf("listen error %d \n", errno);
		perror("listern error msg :");
		return 1;
	}
	while(!done){
		printf("waiting for connection \n");
		
		newfd = accept(listenfd, &inaddr, sizeof(struct sockaddr_in));
		if(newfd < 0){
			printf("accept error : %d \n"m, errno);
			perror("accept error : ");
			return;
		}

		memset(buffer, 0, sizeof(buffer));
		if((receiveLen = recv(newfd, buffer, 2048, 0)) < 0){
			printf("recv error : %d \n"m, errno);
			perror("recv error : ");
			return;
		}
		
		printf("recv data : buffer = %s\n", buffer);
		handleReceiveData(buffer);
		if(strstr(buffer, "exit") != NULL){
			done = 1;
		}
		close(newfd);
	}
}

int handleReceiveData(char *buffer){
	return 0;
}