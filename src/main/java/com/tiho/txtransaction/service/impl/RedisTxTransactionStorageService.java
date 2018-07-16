package com.tiho.txtransaction.service.impl;

import com.tiho.txtransaction.entity.TransactionData;
import com.tiho.txtransaction.service.TxTransactionStorageService;
import com.tiho.txtransaction.support.redis.ProtobufRedisTemplate;
import com.tiho.txtransaction.util.ProtobufUtil;
import com.tiho.txtransaction.util.RedisKeyUtil;
import io.protostuff.MessageCollectionSchema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;

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
