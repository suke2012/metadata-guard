package com.acme.demo.converters;

import com.acme.core.metadata.AbstractConverter;
import com.acme.core.metadata.DataConverter;
import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.demo.dto.Account;
import com.acme.demo.dto.CreditAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户档案转换器
 * 处理Account + CreditAccount组合
 */
public class UserProfileConverter extends AbstractConverter {
    
    @Override
    public List<MetadataCollectionUnit> convert(Object... args) {
        List<MetadataCollectionUnit> units = new ArrayList<>();
        
        Account account = null;
        CreditAccount creditAccount = null;
        
        // 解析参数
        for (Object arg : args) {
            if (arg instanceof Account) {
                account = (Account) arg;
            } else if (arg instanceof CreditAccount) {
                creditAccount = (CreditAccount) arg;
            }
        }
        
        if (account == null) {
            return units; // 没有Account对象，无法处理
        }
        
        MetadataCollectionUnit unit = new MetadataCollectionUnit();
        
        // 设置固定字段
        unit.setUserId(account.getUserId());
        unit.setOperateSystem(account.getSystem());
        unit.setProdId("USER_PROFILE");
        
        // 处理Account数据
        unit.addMetadataField("accountTime", account.getTime());
        if (account.getExtInfo() != null) {
            account.getExtInfo().forEach((k, v) -> 
                unit.addMetadataField("account." + k, v));
        }
        
        // 处理CreditAccount数据（如果存在）
        if (creditAccount != null && creditAccount.getExtInfo() != null) {
            creditAccount.getExtInfo().forEach((k, v) -> 
                unit.addMetadataField("credit." + k, v));
        }
        
        units.add(unit);
        return units;
    }
    
    
    @Override
    public String getDescription() {
        return "UserProfileConverter: 处理Account和CreditAccount组合";
    }
}