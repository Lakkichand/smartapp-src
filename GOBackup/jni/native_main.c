/************************************************************************/
/* ����native�˵�����
	main���������������socket��ַ�Լ�socket�˿�
*/
/************************************************************************/

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>

#define STDERR_LOG "/sdcard/.error.log"
#define STDOUT_LOG "/sdcard/.normal.log"

#define GO_SUCCESS "GO_SUCCESS"
#define GO_FAILURE "GO_FAILURE"

#define ARG_ERROR "ARG_ERROR"
#define SOCKET_ERROR "SOCKET_ERROR"
#define SOCKET_CONNECT "SOCKET_CONNECT"
#define NATIVE_QUIT "NATIVE_QUIT"

#define DEBUG_FLAG_NATIVE_MAIN 0

#define BACKUP_APP 1
#define BACKUP_WIFI 2
#define BACKUP_ACCOUNT 3
#define RESTORE_APP 4
#define RESTORE_WIFI 5
#define RESTORE_ACCOUNT 6
#define CHECK_SU 7
#define EXIT 8

struct Cmd{
	int cmd;
	char *cmd_str;
}Cmd;

int main(int argc, char **argv){
	char *address;
	int port;
	int connect_fd;

	if(argc != 3){
		fprintf(stderr, "%s \n", ARG_ERROR);
		return 1;
	}

	address = argv[1];
	port = atoi(argv[2]);
	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("address = %s, port = %d\n", address, port);
	}

	if(port == 0 || address == NULL){
		printf("unvalid arg! address = %s, port = %d\n", address, port);
		return 1;
	}

	connect_fd = connect_to_service(address, port);
	if(connect_fd == -1){
		fprintf(stderr, "%s\n", SOCKET_ERROR);
		return 1;
	}

	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("%s\n", SOCKET_CONNECT);
	}

	//l�ӷ���˳ɹ�������socketͨѶ
	begin_handle_socket(connect_fd);

	//socketͨѶ����ر��׽���
	close(connect_fd);
	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("%s\n", "�Ѿ��ر��׽��֣�socketl�ӶϿ���\n");
	}

	//�˳�main����
	fprintf(stdout, "%s\n", NATIVE_QUIT);
	return 0;
}

int connect_to_service(char *address, int port){
	struct sockaddr_in service_addr;
	int rc;
	int connect_fd;

	if((connect_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1){
		//����l���׽���ʧ��
		perror("socket : ");
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("����l��socketʧ�ܣ�\n");
		}
		return connect_fd;
	}

	bzero(&service_addr, sizeof(service_addr));
	service_addr.sin_family = AF_INET;
	service_addr.sin_addr.s_addr = inet_addr(address);
	service_addr.sin_port = htons(port);
	
	if(connect(connect_fd, (struct sockaddr *)&service_addr,sizeof(struct sockaddr)) == -1) { 
		perror("socket : ");
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("socketl��ʧ�ܣ�\n");
		}
		return -1;
	} 

	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("socketl�ӳɹ���\n");
	}

// 	if((rc =send(connect_fd, "connect success", 15, 0)) == -1){
// 		perror("socket");
// 	}
	return connect_fd;
}

#define MAX_BUF_LEN 2048
int begin_handle_socket(int connect_fd){
	int quit = 0;
	char recv_buffer[MAX_BUF_LEN];
	char temp_buffer[2048];
	char excu_cmd_buf[2048];
	int rc;
	int error_happen = 0;
	int result = 0;
	struct Cmd backup_cmd;

	do{
		rc = recv(connect_fd, recv_buffer, MAX_BUF_LEN, 0);
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("begin_handle_socket : in while : recv : rc = %d\n", rc);
		}
		if(rc == -1){
			error_happen = 1;
			fprintf(stderr, "socket recv : error happen!");
			perror("begin_handle_socket : socket ");
			break;
		}
		if(rc == 0){
			//socket�ر�
			fprintf(stdout, "socket has close!\n");
			break;
		}
		recv_buffer[rc] = '\0';
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("handleCmd : recv : msg = %s\n", recv_buffer);
		}

		//��������
		parse_cmd(recv_buffer, &backup_cmd);
		
		if(backup_cmd.cmd == EXIT){
			//�˳�socket
			quit = 1;
			break;
		}else{
			memset(temp_buffer, 0, sizeof(temp_buffer));
			//��������
			result = do_cmd(backup_cmd);
			if(result == 1){
				//�ɹ�
				get_stdout_msg(excu_cmd_buf, sizeof(excu_cmd_buf));
				sprintf(temp_buffer, "%d:%s:%s\n", backup_cmd.cmd, backup_cmd.cmd_str, excu_cmd_buf);
			}else if(result == 0){
				//ʧ��
				sprintf(temp_buffer, "%d:%s:%s\n", backup_cmd.cmd, backup_cmd.cmd_str, GO_FAILURE);
			}else if(result == -1){
				sprintf(temp_buffer, "unknown cmd, %s\n", GO_FAILURE);
			}
			if(socket_send(connect_fd, temp_buffer) == -1){
				//����ʧ�ܣ��п���socketl���ѶϿ����˳�
				quit = 1;
			}
		}
	}while(!quit);
	return error_happen ? 1 : 0;
}

int socket_send(int connect_fd, char *buffer){
	int rc;
	if((rc =send(connect_fd, buffer, strlen(buffer), 0)) == -1){
		perror("socket");
	}
	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("socket_send : send %s, result = %d\n", buffer, rc);
	}
	return rc;
}

int parse_cmd(char *buffer, struct Cmd *cmd){
	char* s;
	char cmdIndex[4];

	s = strchr(buffer, ':');
	if(s == NULL){
		return 1;
	}
	strncpy(cmdIndex, buffer, 4);
	cmd->cmd = atoi(cmdIndex);
	cmd->cmd_str = ++s;
	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("cmd = %d, cmd_str = %s\n", cmd->cmd, cmd->cmd_str);
	}
	return 0;
}

int do_cmd(struct Cmd cmd){
	char buffer[2048];
	int result = 0;
	switch(cmd.cmd){
	case BACKUP_APP:
	case BACKUP_WIFI:
	case BACKUP_ACCOUNT:
	case RESTORE_APP:
	case RESTORE_WIFI:
	case RESTORE_ACCOUNT:
	case CHECK_SU:
		sprintf(buffer, "1>%s 2>%s %s", STDOUT_LOG, STDERR_LOG, cmd.cmd_str);
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("do_cmd : buffer = %s\n", buffer);
		}
		system(buffer);
		result = analysis_excu_cmd_result();
		break;
	case EXIT:
		result = 1;
		break;
	default:
		result = -1;
		break;
	}
	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("do_cmd : result = %d\n", result);
	}
	return result;
}

/* ���ɹ�����1�� ���󷵻�0*/
int analysis_excu_cmd_result(){
	FILE* fp = NULL;
	char buffer[4096];
	int error_happen = 0;
	int rc;
	int result = 0;

	//����׼�������
	fp = fopen(STDERR_LOG, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, sizeof(buffer) -1, fp);
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("analysis_excu_cmd_result : STDERR_LOG : rc = %d\n", rc);
		}
		if(rc > 0){
			buffer[rc] = '\0';
			if(DEBUG_FLAG_NATIVE_MAIN){
				printf("analysis_excu_cmd_result : STDERR_LOG : buffer = %s\n", buffer);
			}
			if(strstr(buffer, GO_FAILURE) != NULL){
				error_happen = 1;
			}
		}
		close(fp);
	}

	if(error_happen){
		//������
		return 0;
	}

	//��׼�������û�д��󣬼���ע���
	fp = fopen(STDOUT_LOG, "rb");
	if(fp != NULL){
		rc = fread(buffer, 1, sizeof(buffer) -1, fp);
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("analysis_excu_cmd_result : STDOUT_LOG : rc = %d\n", rc);
		}
		if(rc > 0){
			buffer[rc] = '\0';
			if(DEBUG_FLAG_NATIVE_MAIN){
				printf("analysis_excu_cmd_result : STDOUT_LOG : buffer = %s\n", buffer);
			}
			if(strstr(buffer, GO_SUCCESS) != NULL){
				result = 1;
			}
		}
		close(fp);
	}
	return result;
}

int get_stdout_msg(char *buf, int buf_len){
	FILE* fp = NULL;
	int rc;
	int result = 1;

	if(DEBUG_FLAG_NATIVE_MAIN){
		printf("get_stdout_msg : %s, %d\n", buf, buf_len);
	}
	fp = fopen(STDOUT_LOG, "rb");
	if(fp != NULL){
		rc = fread(buf, 1, buf_len -1, fp);
		if(DEBUG_FLAG_NATIVE_MAIN){
			printf("get_stdout_msg : STDOUT_LOG : rc = %d\n", rc);
		}
		if(rc > 0){
			buf[rc] = '\0';
			if(DEBUG_FLAG_NATIVE_MAIN){
				printf("get_stdout_msg : STDOUT_LOG : buf = %s\n", buf);
			}
			result = 0;
		}
		close(fp);
	}
	return result;
}
