package com.alpha.apiautobot.domain.dao.kucoin;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.alpha.apiautobot.domain.dao.kucoin.Market;

import com.alpha.apiautobot.domain.dao.kucoin.MarketDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig marketDaoConfig;

    private final MarketDao marketDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        marketDaoConfig = daoConfigMap.get(MarketDao.class).clone();
        marketDaoConfig.initIdentityScope(type);

        marketDao = new MarketDao(marketDaoConfig, this);

        registerDao(Market.class, marketDao);
    }
    
    public void clear() {
        marketDaoConfig.clearIdentityScope();
    }

    public MarketDao getMarketDao() {
        return marketDao;
    }

}
