package com.tiho.txtransaction.proxy;

import com.alipay.sofa.rpc.common.utils.ClassLoaderUtils;
import com.tiho.txtransaction.transport.RpcTransport;

import java.lang.reflect.Proxy;

public class ProxyInvokerUtil {

    public static <T> T getInvoker(Class<T> clazz, RpcTransport rpcTransport) {
        ClassLoader classLoader = ClassLoaderUtils.getCurrentClassLoader();
        T obj = (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz}, new RpcInvocationHandler(clazz, rpcTransport));
        return obj;
    }
}
