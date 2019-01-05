/**
 * @version 1.0.0  单向通信需求：服务端接受客户端发送消息，客户端随意发送消息。
 *                        实现：客户端每次都重新建立连接。
 *                 {@link myOwnTest.SocketServer} 作为服务器，可以一直保持开启，接收来自于客户端的消息
 *                 {@link myOwnTest.SocketClient} 作为客户端，可以实现连接服务端并且输入数据传递给服务端，第25行的注释很重要。
 *
 * @version 1.0.1 双向通信
 *                {@link myOwnTest.SocketChatServer} 作为服务器端，
 *                {@link myOwnTest.SocketChatClient} 作为客户端
 *                客户端在发送完消息后，需要服务端进行处理并返回。
 *
 */
package myOwnTest;