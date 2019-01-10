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
 * @version 2.0.0 基于netty的双向通信
 *               {@link myOwnTest.HelloWordServer} 作为服务端
 *               {@link myOwnTest.HelloWorldClient} 作为客户端
 *
 * @version 2.0.1 netty的channelActive顺序事件
 *               {@link myOwnTest.HWClient} 作为客户端
 *               {@link myOwnTest.HWServer} 作为服务端
 *               netty的事件机制是由前至后的，
 *               一般来说，都是一个channel的ChannnelActive方法中调用fireChannelActive来触发调用下一个handler中的ChannelActive方法，
 *               即你在ChannelPipeline中添加handler的时候，
 *               要在第一个handler的channelActive方法中调用fireChannelActive，以此来触发下一个事件。
 *               我们再来写一个案例说明一下：
 */
package myOwnTest;