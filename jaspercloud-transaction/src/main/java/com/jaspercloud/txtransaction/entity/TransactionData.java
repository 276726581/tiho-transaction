package com.jaspercloud.txtransaction.entity;

import java.util.Arrays;

public class TransactionData {

    private String txId;
    private String serviceName;
    private String className;
    private String methodName;
    private Object[] args;
    private String[] argsType;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String[] getArgsType() {
        return argsType;
    }

    public void setArgsType(String[] argsType) {
        this.argsType = argsType;
    }

    public TransactionData() {
    }

    public TransactionData(String txId, String serviceName, String className, String methodName, Object[] args, String[] argsType) {
        this.txId = txId;
        this.serviceName = serviceName;
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.argsType = argsType;
    }

    public static String getTransactionDataKey(TransactionData data) {
        StringBuilder builder = new StringBuilder();
        builder.append(data.getClassName());
        builder.append(".");
        builder.append(data.getMethodName());
        builder.append("(");
        if (null != data.getArgsType()) {
            for (String type : data.getArgsType()) {
                builder.append(type);
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        String key = builder.toString();
        return key;
    }

    @Override
    public String toString() {
        return "TransactionData{" +
                "txId='" + txId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", argsType=" + Arrays.toString(argsType) +
                '}';
    }
}
