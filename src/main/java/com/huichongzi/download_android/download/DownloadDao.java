package com.huichongzi.download_android.download;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * 下载数据库类，用于保存及获取下载列表信息
 * Created by cuihz on 2014/7/7.
 */
class DownloadDao {
    private static final Logger logger = LoggerFactory.getLogger(DownloadDao.class);

    protected static void delete(Context context, int id){
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.deleteById(id);
        }
        catch (SQLException e){
            logger.error("delete failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static void deleteList(Context context, List<DownloadInfo> infos){
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            for(DownloadInfo info : infos) {
                dao.delete(info);
            }
        }
        catch (SQLException e){
            logger.error("delete failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static void save(Context context, DownloadInfo info){
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.createOrUpdate(info);
        }
        catch (SQLException e){
            logger.error("add failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }


    protected static void saveList(Context context, List<DownloadInfo> infos){
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            for(DownloadInfo info : infos) {
                dao.createOrUpdate(info);
            }
        }
        catch (SQLException e){
            logger.error("add failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static List<DownloadInfo> getList(Context context, String group, boolean isDowned) throws DownloadDBException{
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
            logger.error("add failed, {}", e.getMessage());
            throw new DownloadDBException("failed to get download list from db");
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

}
