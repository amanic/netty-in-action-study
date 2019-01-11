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
 *
 * @version 3.0.0 关于拆包粘包
 *               {@link myOwnTest.AboutPackageServer} 作为服务端
 *               {@link myOwnTest.AboutPackageClient} 作为客户端
 *
 * @version 3.0.1 关于解决拆包粘包，
 *
 *               上层应用协议为了对消息进行区分，一般采用如下4种方式：
 *
 *               1、消息长度固定，累计读取到消息长度总和为定长Len的报文之后即认为是读取到了一个完整的消息。计数器归位，重新读取。
 *               2、将回车换行符作为消息结束符。
 *               3、将特殊的分隔符作为消息分隔符，回车换行符是他的一种。
 *               4、通过在消息头定义长度字段来标识消息总长度。
 *               LineBasedframeDecoder属于第二种，
 *               今天我们要说的DelimiterBasedFrameDecoder和FixedLengthFrameDecoder属于第三种和第一种。
 *               DelimiterBasedFrameDecoder用来解决以特殊符号作为消息结束符的粘包问题，FixedLengthFrameDecoder用来解决定长消息的粘包问题。
 *               下面首先来用DelimiterBasedFrameDecoder来写一个例子，我们看一下效果然后接着分析用法。
 *               {@link myOwnTest.PackageSolutionClient} 作为客户端
 *               {@link myOwnTest.PackageSolutionServer} 作为服务端
 *               启动服务端和客户端，我们能看到服务端接收客户端发过来的消息一共分17次接收。那么为什么是17次呢？而且我们并没有使用在上一篇中解决拆包和粘包问题的LineBasedFrameDecoder，并且这次我们的消息每一行的末尾也换成了”\t”。下面就来讲解一下DelimiterBasedFrameDecoder的使用。
 *
 *               DelimiterBasedFrameDecoder是将特殊的字符作为消息的分隔符，本例中用到的是”\t”。而LineBasedFrameDecoder是默认将换行符”\n”作为消息分隔符。首先我们注意到在ServerChannelInitializer中我们在添加解码器时跟以前有点不一样：
 *
 *               ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
 *               pipeline.addLast("framer", new DelimiterBasedFrameDecoder(2048, delimiter));
 *               这里我们添加DelimiterBasedFrameDecoder解码器并且手动指定消息分隔符为：”\t”。我们可以看一下DelimiterBasedFrameDecoder的构造方法：
 *
 *               public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter) {
 *                       this(maxFrameLength, stripDelimiter, true, delimiter);
 *               }
 *               maxFrameLength：解码的帧的最大长度
 *
 *               stripDelimiter：解码时是否去掉分隔符
 *
 *               failFast：为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异常
 *
 *               delimiter：分隔符
 *
 *               这个时候大家应该明白了为什么服务端分17次收到消息。我们在消息的每一行都加了一个”\t”,自然解码器在度消息时遇到”\t”就会认为这是一条消息的结束。用这种方式我们可以把”\t”换成任何我们自定义的字符对象。换成”\n”也是可以的。
 *
 *               {@link io.netty.handler.codec.FixedLengthFrameDecoder}使用
 *               FixedLengthFrameDecoder是固定长度解码器，它能够按照指定的长度对消息进行自动解码。使用它也没有什么特别费力的事情，在ServerChannelInitializer类中添加：
 *
 *               pipeline.addLast(new FixedLengthFrameDecoder(23));//参数为一次接受的数据长度
 *
 *               即可，同时也别忘了把刚才使用的DelimiterBasedFrameDecoder注释掉啊，不然达不到效果。
 *
 * @version 3.0.2 使用{@link io.netty.handler.codec.LengthFieldBasedFrameDecoder}来解决接收包长度问题
 *               {@link myOwnTest.CustomServer} 作为服务端
 *               {@link myOwnTest.CustomClient} 作为客户端
 *               1. maxFrameLength - 发送的数据帧最大长度（超出后会做一些特殊处理）
 *
 *               2. lengthFieldOffset - 定义长度域位于发送的字节数组中的下标。换句话说：发送的字节数组中下标为${lengthFieldOffset}的地方是长度域的开始地方
 *
 *               3. lengthFieldLength - 用于描述定义的长度域的长度。换句话说：发送字节数组bytes时, 字节数组bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength]域对应于的定义长度域部分
 *
 *               4. lengthAdjustment - 满足公式: 发送的字节数组bytes.length - lengthFieldLength = bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength] + lengthFieldOffset + lengthAdjustment 
 *
 *               5. initialBytesToStrip - 接收到的发送数据包，去除前initialBytesToStrip位
 *
 *               6. failFast - true: 读取到长度域超过maxFrameLength，就抛出一个 TooLongFrameException。false: 只有真正读取完长度域的值表示的字节之后，才会抛出 TooLongFrameException，默认情况下设置为true，建议不要修改，否则可能会造成内存溢出
 *
 *               7. ByteOrder - 数据存储采用大端模式或小端模式
 *
 *
 */
package myOwnTest;