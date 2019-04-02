package myOwnRPC.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import myOwnRPC.ClassInfo;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @auther chen.haitao
 * @date 2019-04-02
 */
public class RPCServerHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取调用信息，寻找服务实现类
        ClassInfo classInfo = (ClassInfo) msg;
        String implName = getImplClassName(classInfo.getClassName());
        Class<?> clazz = Class.forName(implName);
        Method method = clazz.getMethod(classInfo.getMethodName(), classInfo.getParamTypes());
        Object result = method.invoke(clazz.newInstance(), classInfo.getParams());
        ctx.writeAndFlush(result);
    }

    private String getImplClassName(String interfaceName) throws ClassNotFoundException {
        Class interClass = Class.forName(interfaceName);
        String servicePath = "myOwnRPC";
        Reflections reflections = new Reflections(servicePath);
        Set<Class> implClasses = reflections.getSubTypesOf(interClass);
        if (implClasses.isEmpty()) {
            System.err.println("impl class is not found!");
        } else if (implClasses.size() > 1) {
            System.err.println("there are many impl classes, not sure invoke which");
        } else {
            Class[] classes = implClasses.toArray(new Class[1]);
            return classes[0].getName();
        }
        return null;
    }
}
