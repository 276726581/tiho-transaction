package com.tiho.txtransaction.proxy;

import com.alipay.sofa.rpc.codec.SerializerFactory;
import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.common.RpcOptions;
import com.alipay.sofa.rpc.common.utils.ClassUtils;
import com.alipay.sofa.rpc.core.exception.RpcErrorType;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.message.MessageBuilder;
import com.tiho.txtransaction.transport.RpcTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcInvocationHandler implements InvocationHandler {

    private Class clazz;
    private RpcTransport rpcTransport;
    private static Byte serializeType;

    static {
        serializeType = parseSerializeType();
    }

    public RpcInvocationHandler(Class clazz, RpcTransport rpcTransport) {
        this.clazz = clazz;
        this.rpcTransport = rpcTransport;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && paramTypes.length == 0) {
            return clazz.toString();
        } else if ("hashCode".equals(methodName) && paramTypes.length == 0) {
            return clazz.hashCode();
        } else if ("equals".equals(methodName) && paramTypes.length == 1) {
            return clazz.equals(args);
        }
        SofaRequest sofaRequest = MessageBuilder.buildSofaRequest(method.getDeclaringClass(), method, paramTypes, args);
        decorateRequest(sofaRequest);
        SofaResponse response = rpcTransport.invoke(sofaRequest);
        if (response.isError()) {
            throw new SofaRpcException(RpcErrorType.SERVER_UNDECLARED_ERROR, response.getErrorMsg());
        }
        Object ret = response.getAppResponse();
        if (ret instanceof Throwable) {
            throw (Throwable) ret;
        } else {
            if (ret == null) {
                return ClassUtils.getDefaultPrimitiveValue(method.getReturnType());
            }
            return ret;
        }
    }

    private void decorateRequest(SofaRequest sofaRequest) {
        sofaRequest.setTargetServiceUniqueName(clazz.getName());
        sofaRequest.setSerializeType(serializeType == null ? 0 : serializeType);
    }

    private static Byte parseSerializeType() {
        String serialization = RpcConfigs.getStringValue(RpcOptions.DEFAULT_SERIALIZATION);
        Byte serializeType = SerializerFactory.getCodeByAlias(serialization);
        if (serializeType == null) {
            throw new SofaRpcRuntimeException("Unsupported serialization type: " + serialization);
        }
        return serializeType;
    }
}
