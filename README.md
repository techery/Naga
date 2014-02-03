Naga is not a framework.
====

Naga aims to be a very small NIO library that provides a handful of java classes to wrap the usual Socket and ServerSocket with asynchronous NIO counterparts (similar to NIO2 planned for Java 1.7).

All of this is driven from a single thread, making it useful for both client (e.g. allowing I/O to be done in the AWT-thread without any need for threads) and server programming (1 thread for all connections instead of 2 threads/connection).

Internally Naga is a straightforward NIO implementation without any threads or event-queues thrown in, it is "just the NIO-stuff", to let you build things on top of it.

Naga contains the code needed to get NIO up and running without having to code partially read buffers and setting various selection key flags.

Here is an example opening a server socket:

```java
NIOService service = new NIOService;
NIOServerSocket serverSocket = service.openServerSocket(1234);
serverSocket.setConnectionAcceptor(myAcceptor);
serverSocket.listen(myObserver);
```

This is how working with a regular socket looks like:

```java
NIOService service = new NIOService;
NIOSocket serverSocket = service.openSocket("www.google.com", 1234);
socket.listen(myObserver);
// Asynchronous write by default:
socket.write("Some message".getBytes());
```
