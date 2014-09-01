package com.huichongzi.download_android.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cuihz on 14-7-25.
 */
class DownloadDBHelper extends OrmLiteSqliteOpenHelper {

    private static final Logger logger = LoggerFactory.getLogger(DownloadDBHelper.class);

    private static final String DOWNLOAD_DB_NAME = "download.db";
    private static final int DOWNLOAD_DB_VERSION = 1;

    private static final Class<?>[] DATA_CLASSES = {DownloadInfo.class};

    // the DAO object we use to access the tables
    private Dao<DownloadInfo, Integer> downloadDao;

    private static final AtomicInteger usageCounter = new AtomicInteger(0);
    private static DownloadDBHelper helper = null;

    public DownloadDBHelper(Context context) {
        super(context, DOWNLOAD_DB_NAME, null, DOWNLOAD_DB_VERSION);
    }

    /**
     * Get the helper, possibly constructing it if necessary. For each call to this method, there should be 1 and only 1
     * call to {@link #close()}.
     */
    public static synchronized DownloadDBHelper getHelper(Context context) {
        if (helper == null) {
            helper = new DownloadDBHelper(context);
        }
        usageCounter.incrementAndGet();
        return helper;
    }

    /**
     * This is called when the database is first created. Usually you should call createTable
     * statements here to create the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            for (Class<?> dataClass : DATA_CLASSES) {
                logger.info("DBHelper.onCreate:{}",dataClass.getName());
                TableUtils.createTable(connectionSource, dataClass);
            }
        } catch (Throwable e) {
            logger.error(e.toString(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows
     * you to adjust the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            logger.info("db onUpgrade,dbname:{},oldVersion:{},newVersion:{}",DOWNLOAD_DB_NAME,oldVersion,newVersion);
            for (Class<?> dataClass : DATA_CLASSES) {
                logger.info("DBHelper.onUpgrade:{}",dataClass.getName());
                TableUtils.dropTable(connectionSource, dataClass, true);
            }
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            logger.error(e.toString(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our SmartCacheModel class. It will create it or just give the cached
     * value.
     */
    Dao<DownloadInfo, Integer> getDownloadDao() throws SQLException {
        if (null == downloadDao) {
            downloadDao = getDao(DownloadInfo.class);
        }
        return downloadDao;
    }

    /**
     * Close the database connections and clear any cached DAOs. For each call to {@link #getHelper(android.content.Context)}, there
     * should be 1 and only 1 call to this method. If there were 3 calls to {@link #getHelper(android.content.Context)} then on the 3rd
     * call to this method, the helper and the underlying database connections will be closed.
     */
    @Override
    public void close() {
        if (usageCounter.decrementAndGet() == 0) {
            super.close();
            downloadDao = null;
            helper = null;
        }
    }

}