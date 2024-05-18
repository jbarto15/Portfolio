#include <iostream>
#include <readline/readline.h>
#include "shelpers.hpp"


int main() {

    std::string line;
    while ( char *buffer = readline("% ")) {

        line = buffer;

        if ( strlen(buffer) > 0 ) {
            add_history(buffer);
        }

        // If the command given is exit then we want to break out of the while loop and have the shell program finish
        if ( line == "exit" ) {
            break;
        }

        // Break up the string into a vector of strings
        std::vector<std::string> parts = tokenize(line);

        // Call getCommands
        std::vector<Command> commands = getCommands(parts);

        // Handle cd
        if ( parts[0] == "cd" ) {

            const char *file_path;

            if ( parts.size() == 1 ) {
                file_path = getenv("HOME");
            } else if ( parts.size() == 2 ) {
                file_path = parts[1].c_str();
            }

            // Call chdir
            int cd_call = chdir(file_path);

            // Error check for chdir
            if ( cd_call == -1 ) {
                perror("Error when calling chdir");
                exit(1);
            }


        } else {

            // Loop through commands
            for ( Command command: commands ) {

                // Create a child process
                int pid = fork();

                // Error check if fork worked correctly
                if ( pid == -1 ) {
                    perror("Error when calling fork");
                    exit(1);
                }

                // Execute the command in the child process
                if ( pid == 0 ) {

                    if ( command.outputFd != STDOUT_FILENO ) {
                        // Make the dup call and change the standard output to the command's output file descriptor
                        int dup_call = dup2(command.outputFd, STDOUT_FILENO);

                        // Error check if dup2 worked correctly
                        if ( dup_call == -1 ) {
                            perror("Error when calling dup2");
                            exit(1);
                        }
                    }

                    if ( command.inputFd != STDIN_FILENO ) {
                        // Make the dup call and change the standard input to the command's input file descriptor
                        int dup_call = dup2(command.inputFd, STDIN_FILENO);

                        // Error check if dup2 worked correctly
                        if ( dup_call == -1 ) {
                            perror("Error when calling dup2");
                            exit(1);
                        }
                    }

                    // Execute the command received
                    int exec_call = execvp(command.argv[0], const_cast<char *const *>(command.argv.data()));

                    // Error check if execvp worked correctly
                    if ( exec_call == -1 ) {
                        perror("Error when calling exec");
                        exit(1);
                    }

                } else {
                    // In the parent process, wait for the child process to finish
                    int status;
                    int wait_call = waitpid(pid, &status, 0);

                    // Error check if wait worked correctly
                    if ( wait_call == -1 ) {
                        perror("Error when calling wait");
                        exit(1);
                    }

                    // If the output has changed in the child process then close the output descriptor the child was using
                    if ( command.outputFd != STDOUT_FILENO ) {
                        int close_output = close(command.outputFd);

                        // Error check if close worked correctly
                        if ( close_output == -1 ) {
                            perror("Error when calling close");
                            exit(1);
                        }
                    }

                    // If the input has changed in the child process then close the input descriptor the child was using
                    if ( command.inputFd != STDIN_FILENO ) {
                        int close_input = close(command.inputFd);

                        // Error check if close worked correctly
                        if ( close_input == -1 ) {
                            perror("Error when calling close");
                            exit(1);
                        }
                    }
                }
            }
        }
        free(buffer);
    }

    return 0;
}
