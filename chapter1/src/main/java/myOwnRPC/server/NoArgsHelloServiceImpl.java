package myOwnRPC.server;

import myOwnRPC.NoArgsHelloService;

/**
 * @auther chen.haitao
 * @date 2019-04-02
 */
public class NoArgsHelloServiceImpl implements NoArgsHelloService {
    @Override
    public String hello() {
        return "hello";
    }
}
