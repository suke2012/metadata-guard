package com.acme.demo.converters;

import com.acme.core.metadata.AbstractConverter;
import com.acme.core.metadata.DataConverter;
import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.demo.dto.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据转换器
 * 处理单个Account对象
 */
public class UserDataConverter extends AbstractConverter {
    
    @Override
    public List<MetadataCollectionUnit> convert(Object... args) {
        List<MetadataCollectionUnit> units = new ArrayList<>();
        
        for (Object arg : args) {
            if (arg instanceof Account) {
                Account account = (Account) arg;
                
                MetadataCollectionUnit unit = new MetadataCollectionUnit();
                
                // 设置固定字段
                unit.setUserId(account.getUserId());
                unit.setOperateSystem(account.getSystem());
                unit.setProdId("USER_ACCOUNT");
                
                // 设置动态字段
                unit.addMetadataField("accountTime", account.getTime());
                
                // 处理extInfo
                if (account.getExtInfo() != null) {
                    account.getExtInfo().forEach((k, v) -> 
                        unit.addMetadataField("user." + k, v));
                }
                
                units.add(unit);
            }
        }
        
        return units;
    }
    
    
    @Override
    public String getDescription() {
        return "UserDataConverter: 处理单个Account对象";
    }
}