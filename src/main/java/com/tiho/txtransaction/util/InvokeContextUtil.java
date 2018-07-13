package com.tiho.txtransaction.util;

import com.alipay.remoting.InvokeContext;
import com.alipay.sofa.rpc.common.RemotingConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;

public final class InvokeContextUtil {

    private InvokeContextUtil() {

    }

    public static InvokeContext createInvokeContext(SofaRequest request) {
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, request.getSerializeType());
        invokeContext.put(RemotingConstants.HEAD_TARGET_SERVICE, request.getTargetServiceUniqueName());
        invokeContext.put(RemotingConstants.HEAD_METHOD_NAME, request.getMethodName());
        String genericType = (String) request.getRequestProp(RemotingConstants.HEAD_GENERIC_TYPE);
        if (genericType != null) {
            invokeContext.put(RemotingConstants.HEAD_GENERIC_TYPE, genericType);
        }
        return invokeContext;
    }
}
