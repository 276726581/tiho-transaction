package com.jaspercloud.txtransaction.event;

import com.alipay.sofa.rpc.event.Event;

public class OnServiceRegisteredEvent implements Event {

    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public OnServiceRegisteredEvent(String serviceName) {
        this.serviceName = serviceName;
    }
}
