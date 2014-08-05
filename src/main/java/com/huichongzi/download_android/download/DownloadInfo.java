package com.huichongzi.download_android.download;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 下载信息bean类
 * Created by cuihz on 2014/7/3.
 */
@DatabaseTable
public class DownloadInfo implements Serializable{

    /** 唯一标示（如果无Int类型的标示，可以用哈希值，只要保证此id唯一即可） **/
    @DatabaseField(id = true)
    private int id;
    @DatabaseField(canBeNull = false)
	private String name;
    @DatabaseField(canBeNull = false)
    private String downDir;
    @DatabaseField
    private String type;
    @DatabaseField(canBeNull = false)
	private String url;
    @DatabaseField
	private String md5;
    @DatabaseField
	private long size;
    @DatabaseField
    private String group;
    @DatabaseField
    private int checkMode;
    @DatabaseField
    private int reconnMode;
    @DatabaseField
    private int state;
    @DatabaseField
    private boolean unlimite;
    @DatabaseField
    private int progress;
    @DatabaseField
    private String other;
    @DatabaseField(canBeNull = false)
    private long createTime;
    @DatabaseField
    private long downloadTime;

    private long speed;




    /**
     * 获取下载唯一标示
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * 设置下载唯一标示（如果无Int类型的标示，可以用哈希值，只要保证此id唯一即可）
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取文件名
     * @return string
     */
    public String getName() {
        return name;
    }

    /**
     * 设置文件名
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取下载路径（组合后）
     * @return string
     */
    public String getPath() {
        String path = downDir;
        if(!downDir.endsWith("/")){
            path += "/";
        }
        path += name;
        if(type != null && !type.equals("")){
            path += "." + type;
        }
        return path;
    }


    /**
     * 获取下载目录
     * @return string
     */
    public String getDownDir() {
        return downDir;
    }

    /**
     * 设置下载目录
     * @param downDir
     */
    public void setDownDir(String downDir) {
        this.downDir = downDir;
    }

    /**
     * 获取下载文件类型（扩展名,如果包含在文件名内，则返回空）
     * @return string
     */
    public String getType() {
        return type;
    }


    /**
     * 设置下载文件类型（扩展名，如果包含在文件名内，则不必设置或设置为空，避免重复！）
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }


    /**
     * 获取下载url
     * @return string
     */
    public String getUrl() {
        return url;
    }


    /**
     * 设置下载url
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取服务器返回的md5值，用于下载完成校验
     * @return string
     */
    public String getMd5() {
        return md5;
    }


    /**
     * 设置服务器返回的md5值，用于下载完成校验。当校验模式包含“下载后校验md5”，用户必须设置该项
     * @param md5
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }


    /**
     * 获取服务器返回的文件大小。当校验模式包含“下载前校验大小”，此大小为服务器返回的下载信息中的字段；不包含，此大小为请求url后返回的文件大小
     * @return long
     */
    public long getSize() {
        return size;
    }

    /**
     * 这是文件大小。当校验模式包含“下载前校验大小”，用户必须设置该项；不包含，则不必设置，sdk会自动请求url获取文件大小
     * @param size
     */
    public void setSize(long size) {
        this.size = size;
    }


    /**
     * 获取下载组，用于进行批量暂停等操作。
     * @return string
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置下载组，用于进行批量暂停等操作。
     * @param group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取校验模式，具体见DownloadOrder类
     * @return int
     */
    public int getCheckMode() {
        return checkMode;
    }

    /**
     * 设置校验模式，具体见DownloadOrder类
     * @param checkMode
     */
    public void setCheckMode(int checkMode) {
        this.checkMode = checkMode;
    }


    /**
     * 获取重连模式，具体见DownloadOrder类
     * @return int
     */
    public int getReconnMode() {
        return reconnMode;
    }

    /**
     * 设置重连模式，具体见DownloadOrder类
     * @param reconnMode
     */
    public void setReconnMode(int reconnMode) {
        this.reconnMode = reconnMode;
    }

    /**
     * 获取下载状态，具体见DownloadOrder类
     * @return int
     */
    public int getState() {
        return state;
    }

    /**
     * 设置下载状态，具体见DownloadOrder类
     * @param state
     */
    public void setState(int state) {
        this.state = state;
    }



    /**
     * 获取上次保存的下载速度
     * @return long
     */
    public long getSpeed() {
        return speed;
    }

    /**
     * 设置下载速度
     * @param speed
     */
    protected void setSpeed(long speed) {
        this.speed = speed;
    }

    /**
     * 获取上次保存的下载进度，100为满值
     * @return int
     */
    public int getProgress() {
        return progress;
    }

    /**
     * 设置下载进度
     * @param progress
     */
    protected void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * 获取扩展信息
     * @return string
     */
    public String getOther() {
        return other;
    }


    /**
     * 设置扩展信息
     *
     * @param other 扩展信息，如果是多个信息，建议使用分号进行分割
     */
    public void setOther(String other) {
        this.other = other;
    }

    /**
     * 下载是否受限
     * @return boolean
     */
    public boolean isUnlimite() {
        return unlimite;
    }


    /**
     * 设置下载是否受限。为false则受最大下载数限制；否则单独计算，不记入下载数
     * @param unlimite
     */
    public void setUnlimite(boolean unlimite) {
        this.unlimite = unlimite;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }


    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", downDir='" + downDir + '\'' +
                ", type='" + ( type == null ? "" : type ) + '\'' +
                ", url='" + url + '\'' +
                ", md5='" + ( md5 == null ? "" : md5 ) + '\'' +
                ", size=" + size +
                ", other='" + ( other == null ? "" : other ) + '\'' +
                "createTime='" + createTime + '\'' +
                "downloadTime='" + downloadTime + '\'' +
                '}';
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

    /**
     * 检查此bean信息是否有异常
     * @throws IllegalParamsException
     */
    public void checkIllegal() throws IllegalParamsException{
        if(id == 0){
            throw new IllegalParamsException(name + ":id", "must != 0");
        }
        if(downDir == null || downDir.equals("")){
            throw new IllegalParamsException(name + ":downDir", "must not null");
        }
        if(url == null || url.equals("")){
            throw new IllegalParamsException(name + ":url", "must not nul");
        }
        if((checkMode & DownloadOrder.CHECKMODE_SIZE_START) == 1 && size <= 0){
            throw new IllegalParamsException(name + ":checkMode", "if check file size before download, 'size' must > 0");
        }
        if((checkMode & DownloadOrder.CHECKMODE_MD5_END) == 1 && (md5 == null || md5.equals(""))){
            throw new IllegalParamsException(name + ":checkMode", "if check file md5 after download, 'md5' must not null");
        }
    }

}
