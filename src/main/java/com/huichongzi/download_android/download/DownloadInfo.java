package com.huichongzi.download_android.download;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 下载信息bean类
 * Created by cuihz on 2014/7/3.
 */
@DatabaseTable
public class DownloadInfo implements Serializable {

    static final String ID = "id";
    static final String NAME = "name";
    static final String PATH = "path";
    static final String URL = "url";
    static final String MD5 = "md5";
    static final String TOTALSIZE = "totalsize";
    static final String GROUP = "group";
    static final String STATE = "state";
    static final String PRIORITY = "priority";
    static final String CHECKMODE = "checkmode";
    static final String CREATETIME = "createtime";
    static final String DOWNOVERTIME = "downovertime";
    static final String PROGRESS = "progress";


    /** 唯一标示（如果无Int类型的标示，可以用哈希值，只要保证此id唯一即可） **/
    @DatabaseField(columnName = ID, id = true)
    private int id;
    /** 下载任务名字 **/
    @DatabaseField(canBeNull = false, columnName = NAME)
	private String name;
    /** 保存地址 **/
    @DatabaseField(canBeNull = false, columnName = PATH)
    private String path;
    /** 下载地址 **/
    @DatabaseField(canBeNull = false, columnName = URL)
	private String url;
    /** MD5 **/
    @DatabaseField(columnName = MD5)
	private String md5;
    /** 文件大小 **/
    @DatabaseField(columnName = TOTALSIZE)
	private long totalSize;
    /** 下载组 **/
    @DatabaseField(columnName = GROUP)
    private String group;
    /** 优先级,0-1000 **/
    @DatabaseField(columnName = PRIORITY)
    private int priority;
    /** 校验方式 **/
    @DatabaseField(columnName = CHECKMODE)
    private int checkMode;
    /** 下载状态 **/
    @DatabaseField(columnName = STATE)
    private int state;
    /** 创建时间 **/
    @DatabaseField(columnName = CREATETIME)
    private long createTime;
    /** 完成时间 **/
    @DatabaseField(columnName = DOWNOVERTIME)
    private long downOverTime;
    /** 进度 **/
    @DatabaseField(columnName = PROGRESS)
    private int progress;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getCheckMode() {
        return checkMode;
    }

    public void setCheckMode(int checkMode) {
        this.checkMode = checkMode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDownOverTime() {
        return downOverTime;
    }

    public void setDownOverTime(long downOverTime) {
        this.downOverTime = downOverTime;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadInfo that = (DownloadInfo) o;
        if (id != that.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", totalSize=" + totalSize +
                ", group='" + group + '\'' +
                ", priority=" + priority +
                ", checkMode=" + checkMode +
                ", state=" + state +
                ", createTime=" + createTime +
                ", downOverTime=" + downOverTime +
                ", progress=" + progress +
                '}';
    }
}
