echo Compiling Server...

#javac src/shared/Utils.java
#javac src/shared/ProtocolHandler.java
#javac src/server/Server.java
javac src/server/Entrypoint.java

echo Running Server...
java src/server/Entrypoint $1