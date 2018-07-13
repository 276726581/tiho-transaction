package com.tiho.txtransaction.transport;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;

public interface RpcTransport {

    SofaResponse invoke(SofaRequest sofaRequest) throws Exception;
}
