package myOwnRPC.server;

import myOwnRPC.HasArgsHelloService;

/**
 * @auther chen.haitao
 * @date 2019-04-02
 */
public class HasArgsHelloServiceImpl implements HasArgsHelloService {
    @Override
    public String hello(String msg) {
        return msg;
    }
}
