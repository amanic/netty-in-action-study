WebSocket 通过“ Upgrade handshake （升级握手）”从标准的 HTTP 或HTTPS 协议转为 WebSocket。因此，使用 WebSocket 的应用程序将始终以 HTTP/S 开始，然后进行升级。在什么时候发生这种情况取决于具体的应用;它可以是在启动时，或当一个特定的 URL 被请求时。

在我们的应用中，当 URL 请求以“/ws”结束时，我们才升级协议为WebSocket。否则，服务器将使用基本的 HTTP/S。一旦升级连接将使用的WebSocket 传输所有数据。

![image](http://static.open-open.com/lib/uploadImg/20160918/20160918110335_227.jpg)

1.客户端/用户连接到服务器并加入聊天

2.HTTP 请求页面或 WebSocket 升级握手

3.服务器处理所有客户端/用户

4.响应 URI “/”的请求，转到默认 html 页面

5.如果访问的是 URI“/ws” ，处理 WebSocket 升级握手

6.升级握手完成后 ，通过 WebSocket 发送聊天消息

![image](http://static.open-open.com/lib/uploadImg/20160918/20160918110336_991.jpg)

