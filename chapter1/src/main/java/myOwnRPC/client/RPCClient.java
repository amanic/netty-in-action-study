package myOwnRPC.client;

import myOwnRPC.HasArgsHelloService;
import myOwnRPC.NoArgsHelloService;

/**
 * @auther chen.haitao
 * @date 2019-04-02
 */
public class RPCClient {

    public static void main(String[] args){
        NoArgsHelloService noArgsHelloService = (NoArgsHelloService) ServiceProxy.create(NoArgsHelloService.class);
        System.out.println(noArgsHelloService.hello());

        HasArgsHelloService hasArgsHelloService = (HasArgsHelloService) ServiceProxy.create(HasArgsHelloService.class);
        System.out.println(hasArgsHelloService.hello("hello netty rpc"));
    }
}
