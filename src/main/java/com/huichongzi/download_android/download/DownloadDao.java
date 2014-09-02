package com.huichongzi.download_android.download;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 下载数据库类，用于保存及获取下载列表信息
 * Created by cuihz on 2014/7/7.
 */
class DownloadDao {
    private static final Logger logger = LoggerFactory.getLogger(DownloadDao.class);

    protected static void deleteIgnoreException(Context context, int id){
        try {
            delete(context, id);
        } catch (DownloadDBException e) {
            logger.error("", e);
        }
    }

    protected static void delete(Context context, int id) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.deleteById(id);
        }
        catch (SQLException e){
            throw new DownloadDBException("delete failed \n", e);
        }finally {
            helper.close();
        }
    }

    protected static void deleteList(Context context, final List<DownloadInfo> infos) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(DownloadInfo info : infos) {
                        dao.delete(info);
                    }
                    return null;
                }
            });
        }
        catch (SQLException e){
            throw new DownloadDBException("delete list failed \n", e);
        }finally {
            helper.close();
        }
    }


    protected static void saveIgnoreException(Context context, DownloadInfo info){
        try {
            save(context, info);
        } catch (DownloadDBException e) {
            logger.error("", e);
        }
    }

    protected static void save(Context context, DownloadInfo info) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.createOrUpdate(info);
        }
        catch (SQLException e){
            throw new DownloadDBException("add failed \n", e);
        }finally {
            helper.close();
        }
    }


    protected static void saveList(Context context, final List<DownloadInfo> infos) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(DownloadInfo info : infos) {
                        dao.createOrUpdate(info);
                    }
                    return null;
                }
            });
        }
        catch (SQLException e){
            throw new DownloadDBException("add list failed \n", e);
        }finally {
            helper.close();
        }
    }

    protected static List<DownloadInfo> getList(Context context, String group, boolean isDowned) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            QueryBuilder<DownloadInfo, Integer> qb = dao.queryBuilder();
            qb.where().eq("group", group).and();
            if(isDowned){
                qb.where().eq("state", DownloadOrder.STATE_SUCCESS);
                qb.orderBy("downloadTime", true);
            }
            else{
                qb.where().ne("state", DownloadOrder.STATE_SUCCESS);
                qb.orderBy("createTime", true);
            }
            List<DownloadInfo> list = qb.query();
            return list;
        }
        catch (SQLException e){
            throw new DownloadDBException("failed to get download list from db \n", e);
        }finally {
            helper.close();
        }
    }

}
