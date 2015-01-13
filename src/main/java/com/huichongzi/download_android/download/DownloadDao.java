package com.huichongzi.download_android.download;

import android.content.Context;
import com.huichongzi.download_android.exception.DownloadDBException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 下载数据库类，用于保存及获取下载列表信息
 * Created by cuihz on 2014/7/7.
 */
class DownloadDao {
    private static final Logger logger = LoggerFactory.getLogger(DownloadDao.class);


    static void deleteList(Context context, List<DownloadInfo> infos) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.delete(infos);
        }
        catch (Exception e){
            throw new DownloadDBException("delete list failed \n", e);
        }finally {
            helper.close();
        }
    }

    static void delete(Context context, int id) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.deleteById(id);
        }
        catch (Exception e){
            throw new DownloadDBException("delete list failed \n", e);
        }finally {
            helper.close();
        }
    }

    static void deleteByIds(Context context, final List<Integer> ids) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(int id : ids) {
                        dao.deleteById(id);
                    }
                    return null;
                }
            });
        }
        catch (Exception e){
            throw new DownloadDBException("delete list failed \n", e);
        }finally {
            helper.close();
        }
    }



    static void saveIgnoreException(Context context, DownloadInfo info){
        try {
            save(context, info);
        } catch (DownloadDBException e) {
            logger.error(e.toString(), e);
        }
    }

    static void save(Context context, DownloadInfo info) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            dao.createOrUpdate(info);
        }
        catch (Exception e){
            throw new DownloadDBException("add failed \n", e);
        }finally {
            helper.close();
        }
    }

    static boolean has(Context context, int id) throws DownloadDBException{
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            return dao.idExists(id);
        }
        catch (Exception e){
            throw new DownloadDBException("add failed \n", e);
        }finally {
            helper.close();
        }
    }


    static void saveList(Context context, final List<DownloadInfo> infos) throws DownloadDBException {
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
        catch (Exception e){
            throw new DownloadDBException("add list failed \n", e);
        }finally {
            helper.close();
        }
    }




    static void updatePriority(Context context, String group, int id, int priority) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            UpdateBuilder<DownloadInfo, Integer> ub = dao.updateBuilder();
            Where<DownloadInfo, Integer> where = ub.where();
            where.eq(DownloadInfo.ID, id).and();
            where.eq(DownloadInfo.GROUP, group);
            ub.updateColumnValue(DownloadInfo.PRIORITY, priority);
            ub.update();
        }
        catch (Exception e){
            throw new DownloadDBException("failed to updata id \n", e);
        }finally {
            helper.close();
        }
    }

    static void updateByGroup(Context context, String group, int state) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            UpdateBuilder<DownloadInfo, Integer> ub = dao.updateBuilder();
            Where<DownloadInfo, Integer> where = ub.where();
            where.eq(DownloadInfo.GROUP, group).and();
            where.ne(DownloadInfo.STATE, DownloadOrder.STATE_SUCCESS).and();
            where.ne(DownloadInfo.STATE, DownloadOrder.STATE_STOP);
            ub.updateColumnValue(DownloadInfo.STATE, state);
            ub.update();
        }
        catch (Exception e){
            throw new DownloadDBException("failed to updata group \n", e);
        }finally {
            helper.close();
        }
    }

    static void updateByIds(Context context, final String group, final List<Integer> ids, final int state) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(int id : ids) {
                        UpdateBuilder<DownloadInfo, Integer> ub = dao.updateBuilder();
                        Where<DownloadInfo, Integer> where = ub.where();
                        where.eq(DownloadInfo.ID, id).and();
                        where.eq(DownloadInfo.GROUP, group);
                        ub.updateColumnValue(DownloadInfo.STATE, state);
                        ub.update();
                    }
                    return null;
                }
            });
        }
        catch (Exception e){
            throw new DownloadDBException("failed to updata ids \n", e);
        }finally {
            helper.close();
        }
    }


    /**
     * 获取待下载列表（包括下载中），按优先级排序
     * @param context
     * @param group
     * @return
     * @throws DownloadDBException
     */
    static List<DownloadInfo> getRefreshList(Context context, String group) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            QueryBuilder<DownloadInfo, Integer> qb = dao.queryBuilder();
            Where<DownloadInfo, Integer> where = qb.where();
            where.eq(DownloadInfo.GROUP, group).and();
            where.lt(DownloadInfo.STATE, DownloadOrder.STATE_SUCCESS);
            qb.orderBy(DownloadInfo.STATE, true);
            qb.orderBy(DownloadInfo.PRIORITY, false);
            qb.orderBy(DownloadInfo.CREATETIME, true);
            return qb.query();
        }
        catch (Exception e){
            throw new DownloadDBException("failed to get download list from db \n", e);
        }finally {
            helper.close();
        }
    }


    static DownloadInfo getById(Context context, int id) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            return dao.queryForId(id);
        }
        catch (Exception e){
            throw new DownloadDBException("failed to get download from db \n", e);
        }finally {
            helper.close();
        }
    }


    static List<DownloadInfo> getByIds(Context context, final List<Integer> ids) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
            infos = TransactionManager.callInTransaction(helper.getConnectionSource(), new Callable<List<DownloadInfo>>() {
                @Override
                public List<DownloadInfo> call() throws Exception {
                    List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
                    for(int id : ids) {
                        infos.add(dao.queryForId(id));
                    }
                    return infos;
                }
            });
            return infos;
        }
        catch (Exception e){
            throw new DownloadDBException("failed to get download from db \n", e);
        }finally {
            helper.close();
        }
    }



    static List<DownloadInfo> getByGroup(Context context, String group) throws DownloadDBException {
        final DownloadDBHelper helper = DownloadDBHelper.getHelper(context);
        try {
            final Dao<DownloadInfo, Integer> dao = helper.getDownloadDao();
            return dao.queryForEq(DownloadInfo.GROUP, group);
        }
        catch (Exception e){
            throw new DownloadDBException("failed to updata group \n", e);
        }finally {
            helper.close();
        }
    }


}
