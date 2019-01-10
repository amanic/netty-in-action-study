package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class AboutPackageClient {

    private int port;

    public AboutPackageClient(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();
        //
        Bootstrap bootstrap = new Bootstrap();
        Bootstrap noProblemBoot = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ch.pipeline()
                                .addLast("decoder",new StringDecoder())
                                .addLast("encoder",new StringEncoder())
                                .addLast("handler",new ChannelInboundHandlerAdapter(){
                                    private int counter;
                                    private byte[] req = ("Unless required by applicable law or agreed to in writing, software\n" +
                                                "  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                                                "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                                                "  See the License for the specific language governing permissions and\n" +
                                                "  limitations under the License.This connector uses the BIO implementation that requires the JSSE\n" +
                                                "  style configuration. When using the APR/native implementation, the\n" +
                                                "  penSSL style configuration is required as described in the APR/native\n" +
                                                "  documentation.An Engine represents the entry point (within Catalina) that processes\n" +
                                                "  every request.  The Engine implementation for Tomcat stand alone\n" +
                                                "  analyzes the HTTP headers included with the request, and passes them\n" +
                                                "  on to the appropriate Host (virtual host)# Unless required by applicable law or agreed to in writing, software\n" +
                                                "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                                                "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                                                "# See the License for the specific language governing permissions and\n" +
                                                "# limitations under the License.# For example, set the org.apache.catalina.util.LifecycleBase logger to log\n" +
                                                "# each component that extends LifecycleBase changing state:\n" +
                                                "#org.apache.catalina.util.LifecycleBase.level = FINE"
                                        ).getBytes();

                                    //如果服务器端采用了解决方案，需要使用下面的消息，\n的区别
                                    private byte[] noProReq = ("Unless required by applicable law or agreed to in writing, software" +
                                            "  distributed under the License is distributed on an \"AS IS\" BASIS," +
                                            "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                                            "  See the License for the specific language governing permissions and" +
                                            "  limitations under the License.This connector uses the BIO implementation that requires the JSSE" +
                                            "  style configuration. When using the APR/native implementation, the" +
                                            "  penSSL style configuration is required as described in the APR/native" +
                                            "  documentation.An Engine represents the entry point (within Catalina) that processes" +
                                            "  every request.  The Engine implementation for Tomcat stand alone" +
                                            "  analyzes the HTTP headers included with the request, and passes them" +
                                            "  on to the appropriate Host (virtual host)# Unless required by applicable law or agreed to in writing, software" +
                                            "# distributed under the License is distributed on an \"AS IS\" BASIS," +
                                            "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." +
                                            "# See the License for the specific language governing permissions and" +
                                            "# limitations under the License.# For example, set the org.apache.catalina.util.LifecycleBase logger to log" +
                                            "# each component that extends LifecycleBase changing state:" +
                                            "#org.apache.catalina.util.LifecycleBase.level = FINE\n"
                                    ).getBytes();

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        ByteBuf message;


                                        /**
                                         * @version 1
                                         * 将上面的所有字符串作为一个消息体发送出去 这里将会发生拆包
                                         */
//                                        message = Unpooled.buffer(req.length);
//                                        message.writeBytes(req);
//                                        ctx.writeAndFlush(message);


                                        /**
                                         * @version 2
                                         * 将上面的所有字符串作为一个消息体发送出去 这里将会发生粘包
                                         */
                                        for (int i = 0; i < 3; i++) {
                                            message = Unpooled.buffer(req.length);
                                            message.writeBytes(req);
                                            ctx.writeAndFlush(message);
                                        }


                                    }

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        String buf = (String)msg;
                                        System.out.println("Now is : " + buf + " ; the counter is : "+ (++counter));

                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        ctx.close();
                                    }
                                });
                    }
                });


        try {
            ChannelFuture future = bootstrap.connect("127.0.0.1",port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        AboutPackageClient client = new AboutPackageClient(7788);
        client.start();
    }
}
