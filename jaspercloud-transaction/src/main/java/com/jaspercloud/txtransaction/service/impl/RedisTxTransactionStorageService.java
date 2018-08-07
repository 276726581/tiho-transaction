package com.jaspercloud.txtransaction.service.impl;

import com.jaspercloud.txtransaction.entity.TransactionData;
import com.jaspercloud.txtransaction.service.TxTransactionStorageService;
import com.jaspercloud.txtransaction.support.redis.ProtobufRedisTemplate;
import com.jaspercloud.txtransaction.util.ProtobufUtil;
import com.jaspercloud.txtransaction.util.RedisKeyUtil;
import io.protostuff.MessageCollectionSchema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RedisTxTransactionStorageService implements TxTransactionStorageService {

    private static RuntimeSchema<TransactionData> transactionDataSchema = RuntimeSchema.createFrom(TransactionData.class);
    private static MessageCollectionSchema transactionDataListSchema = new MessageCollectionSchema(transactionDataSchema);

    @Autowired
    private ProtobufRedisTemplate protobufRedisTemplate;

    @Override
    public void saveCompensateTransaction(String serviceName, TransactionData transactionData) {
        ListOperations<String, byte[]> forList = protobufRedisTemplate.opsForList();
        String redisKey = RedisKeyUtil.getRedisKey("txtransaction", "compensate", serviceName);
        byte[] bytes = ProtobufUtil.toByteArray(transactionData, transactionDataSchema);
        forList.rightPush(redisKey, bytes);
    }

    @Override
    public void saveCompensateTransaction(List<TransactionData> list) {
        ListOperations<String, byte[]> forList = protobufRedisTemplate.opsForList();
        Map<String, List<TransactionData>> map = list.stream().collect(Collectors.groupingBy(TransactionData::getServiceName));
        for (Map.Entry<String, List<TransactionData>> entry : map.entrySet()) {
            String serviceName = entry.getKey();
            String redisKey = RedisKeyUtil.getRedisKey("txtransaction", "compensate", serviceName);
            List<byte[]> bytesList = list.stream().map(new Function<TransactionData, byte[]>() {
                @Override
                public byte[] apply(TransactionData transactionData) {
                    byte[] bytes = ProtobufUtil.toByteArray(transactionData, transactionDataSchema);
                    return bytes;
                }
            }).collect(Collectors.toList());
            forList.rightPushAll(redisKey, bytesList);
        }
    }

    @Override
    public TransactionData getOneCompensateTransaction(String serviceName) {
        ListOperations<String, byte[]> forList = protobufRedisTemplate.opsForList();
        String redisKey = RedisKeyUtil.getRedisKey("txtransaction", "compensate", serviceName);
        byte[] bytes = forList.leftPop(redisKey);
        if (null == bytes) {
            return null;
        }
        TransactionData transactionData = ProtobufUtil.mergeFrom(bytes, new TransactionData(), transactionDataSchema);
        return transactionData;
    }
}
