package myOwnTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class PackageSolutionClient {

    private int port;

    public PackageSolutionClient(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ByteBuf delimiter = Unpooled.copiedBuffer("\t".getBytes());
                        ch.pipeline()
//                                .addLast("framer", new DelimiterBasedFrameDecoder(2048,delimiter))
                                .addLast(new FixedLengthFrameDecoder(10))
                                .addLast("decoder",new StringDecoder())
                                .addLast("encoder",new StringEncoder())
                                .addLast("",new ChannelInboundHandlerAdapter(){

                                    private byte[] req = ("Unless required by applicable law or agreed to in writing, software\t" +
                                            "  distributed under the License is distributed on an \"AS IS\" BASIS,\t" +
                                            "  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\t" +
                                            "  See the License for the specific language governing permissions and\t" +
                                            "  limitations under the License.This connector uses the BIO implementation that requires the JSSE\t" +
                                            "  style configuration. When using the APR/native implementation, the\t" +
                                            "  penSSL style configuration is required as described in the APR/native\t" +
                                            "  documentation.An Engine represents the entry point (within Catalina) that processes\t" +
                                            "  every request.  The Engine implementation for Tomcat stand alone\t" +
                                            "  analyzes the HTTP headers included with the request, and passes them\t" +
                                            "  on to the appropriate Host (virtual host)# Unless required by applicable law or agreed to in writing, software\t" +
                                            "# distributed under the License is distributed on an \"AS IS\" BASIS,\t" +
                                            "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\t" +
                                            "# See the License for the specific language governing permissions and\t" +
                                            "# limitations under the License.# For example, set the org.apache.catalina.util.LifecycleBase logger to log\t" +
                                            "# each component that extends LifecycleBase changing state:\t" +
                                            "#org.apache.catalina.util.LifecycleBase.level = FINE\t").getBytes();
                                    private int counter;

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        ByteBuf message;

                                        message = Unpooled.buffer(req.length);
                                        message.writeBytes(req);
                                        ctx.writeAndFlush(message);
                                    }

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println("Now is : " + msg.toString() + " ; the counter is : "+ (++counter));
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
        new PackageSolutionClient(7788).start();
    }

}
