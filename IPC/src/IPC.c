/*
 ============================================================================
 Name        : IPC.c
 Author      : Justin Rohr & Kalee Stutzman
 Description : Program that spawns a child that sends user defined signals
 while the parent listens for signals.
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <time.h>

/* Signal handler that determines what signal was sent and reacts appropriately */
void sigHandler(int in) {
	printf("Received signal: ");
	if (in == 16 || in == 10 || in == 30)
		printf("SIGUSR1 \n");
	else if (in == 17 || in == 31 || in == 12)
		printf("SIGUSR2 \n");
	else if (in == 2) {
		printf("^C Shutting Down... \n");
		exit(0);
	}
}

int main(void) {
	pid_t pid, p_pid;
	srand(time(NULL));
	p_pid = getpid();
	pid = fork();

	if (pid < 0) {
		perror("Fork failed");
		exit(errno);
	} else if (pid == 0) { //Child randomly sends signals at different times
		while (1) {
			int r = rand() % 5 + 1;
			sleep(r);
			r = rand() % 2;
			if (r == 0)
				kill(p_pid, SIGUSR1);
			else
				kill(p_pid, SIGUSR2);
		}
	} else { //Parent installs signal handlers and waits for child to send signals
		while (1) {
			printf("Waiting...     ");
			signal(SIGINT, sigHandler);
			signal(SIGUSR1, sigHandler);
			signal(SIGUSR2, sigHandler);
			pause();
		}
	}

	return EXIT_SUCCESS;
}
