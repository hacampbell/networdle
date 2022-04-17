# networdle
A networked implementation of Wordle using TCP Sockets and a custom protocol.

## Running the project

### Server:
- Ensure that startServer.sh has execution access
- Run `./startServer.sh PORT` where `PORT` is a valid port number
    - E.g. `./startServer.sh 53044`

### Client:
- Ensure that startClient.sh has execution access
- Run `./startClient HOST PORT` where `HOST` is the host address of the server that you just started, and `PORT` is the port number you started the server on.
    - E.g. `./startClient.sh 127.0.0.1 53044`

Once finished, you can run `cleanup.sh` to remove the files created during compilation.
