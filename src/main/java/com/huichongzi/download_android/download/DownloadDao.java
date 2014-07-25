package com.huichongzi.download_android.download;

import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载数据库类，用于保存及获取下载列表信息
 * Created by cuihz on 2014/7/7.
 */
class DownloadDao {
    private static final Logger logger = LoggerFactory.getLogger(DownloadDao.class);

    protected static DownloadInfo getById(Context context, int id){
        final DBHelper helper = OpenHelperManager.getHelper(context, DBHelper.class);
        try {
            final Dao<DownloadInfo, String> dao = helper.getDownloadDao();
            return dao.queryForId(id + "");
        }
        catch (SQLException e){
            logger.error("find by id failed, {}", e.getMessage());
            return null;
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static void add(Context context, DownloadInfo info){
        final DBHelper helper = OpenHelperManager.getHelper(context, DBHelper.class);
        try {
            final Dao<DownloadInfo, String> dao = helper.getDownloadDao();
            dao.createIfNotExists(info);
        }
        catch (SQLException e){
            logger.error("add failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static void delete(Context context, int id){
        final DBHelper helper = OpenHelperManager.getHelper(context, DBHelper.class);
        try {
            final Dao<DownloadInfo, String> dao = helper.getDownloadDao();
            dao.deleteById(id + "");
        }
        catch (SQLException e){
            logger.error("delete failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static void update(Context context, DownloadInfo info){
        final DBHelper helper = OpenHelperManager.getHelper(context, DBHelper.class);
        try {
            final Dao<DownloadInfo, String> dao = helper.getDownloadDao();
            dao.createOrUpdate(info);
        }
        catch (SQLException e){
            logger.error("add failed, {}", e.getMessage());
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

    protected static List<DownloadInfo> getList(Context context, String group, boolean isDowned){
        final DBHelper helper = OpenHelperManager.getHelper(context, DBHelper.class);
        try {
            final Dao<DownloadInfo, String> dao = helper.getDownloadDao();
            List<DownloadInfo> list = new ArrayList<DownloadInfo>();
            if(isDowned){
                list = dao.queryBuilder().where().eq("group", group).and().eq("state", DownloadOrder.STATE_SUCCESS).query();
            }
            else{
                list = dao.queryBuilder().where().eq("group", group).and().ne("state", DownloadOrder.STATE_SUCCESS).query();
            }
            return list;
        }
        catch (SQLException e){
            logger.error("add failed, {}", e.getMessage());
            return null;
        }finally {
            OpenHelperManager.releaseHelper();
        }
    }

}
