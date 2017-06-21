package entertainment.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.thunder.entertainment.dao.ChannelModel;

import entertainment.dao.ChannelModelDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig channelModelDaoConfig;

    private final ChannelModelDao channelModelDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        channelModelDaoConfig = daoConfigMap.get(ChannelModelDao.class).clone();
        channelModelDaoConfig.initIdentityScope(type);

        channelModelDao = new ChannelModelDao(channelModelDaoConfig, this);

        registerDao(ChannelModel.class, channelModelDao);
    }
    
    public void clear() {
        channelModelDaoConfig.clearIdentityScope();
    }

    public ChannelModelDao getChannelModelDao() {
        return channelModelDao;
    }

}