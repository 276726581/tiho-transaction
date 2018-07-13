package com.tiho.txtransaction.proxy;

import com.alipay.sofa.rpc.common.cache.ReflectCache;
import com.alipay.sofa.rpc.core.exception.RpcErrorType;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.invoke.Invoker;
import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.message.MessageBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RpcInvoker<T> implements Invoker {

    private T ref;

    public T getRef() {
        return ref;
    }

    public RpcInvoker(Class<T> clazz, T ref) {
        this.ref = ref;
        String className = clazz.getName();
        for (Method m : clazz.getMethods()) {
            ReflectCache.putOverloadMethodCache(className, m);
        }
    }

    @Override
    public SofaResponse invoke(SofaRequest request) throws SofaRpcException {
        String appName = request.getTargetAppName();
        String serviceName = request.getTargetServiceUniqueName();
        String methodName = request.getMethodName();
        Method serviceMethod = ReflectCache.getOverloadMethodCache(serviceName, methodName, request.getMethodArgSigs());
        if (serviceMethod == null) {
            SofaRpcException throwable = cannotFoundServiceMethod(appName, methodName, serviceName);
            SofaResponse response = MessageBuilder.buildSofaErrorResponse(throwable.getMessage());
            return response;
        }
        request.setMethod(serviceMethod);

        SofaResponse sofaResponse = new SofaResponse();
        try {
            Object[] args = request.getMethodArgs();
            Object result = serviceMethod.invoke(ref, args);
            sofaResponse.setAppResponse(result);
        } catch (IllegalArgumentException e) { // 非法参数，可能是实现类和接口类不对应)
            sofaResponse.setErrorMsg(e.getMessage());
        } catch (IllegalAccessException e) { // 如果此 Method 对象强制执行 Java 语言访问控制，并且底层方法是不可访问的
            sofaResponse.setErrorMsg(e.getMessage());
        } catch (InvocationTargetException e) { // 业务代码抛出异常
            sofaResponse.setAppResponse(e.getCause());
        }
        return sofaResponse;
    }

    private SofaRpcException cannotFoundServiceMethod(String appName, String serviceName, String methodName) {
        String errorMsg = LogCodes.getLog(LogCodes.ERROR_PROVIDER_SERVICE_METHOD_CANNOT_FOUND, methodName, serviceName);
        return new SofaRpcException(RpcErrorType.SERVER_NOT_FOUND_INVOKER, errorMsg);
    }
}
