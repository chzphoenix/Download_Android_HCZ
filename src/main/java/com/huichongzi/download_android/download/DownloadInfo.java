package com.huichongzi.download_android.download;

import java.io.Serializable;

/**
 * 下载信息bean类
 * Created by cuihz on 2014/7/3.
 */
public class DownloadInfo implements Serializable{

    private int id;
	private String name;
    private String home;
    private String type;
	private String url;
	private String md5;
	private long size;
    private String group;
    private int mode;
    private int state;
    private String other;




    /**
     * 获取下载唯一标示
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * 设置下载唯一标示
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取文件名
     * @return
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
     * @return
     */
    public String getPath() {
        String path = home + "/" + name;
        if(type != null && !type.equals("")){
            path += "." + type;
        }
        return path;
    }


    /**
     * 获取下载目录
     * @return
     */
    public String getHome() {
        return home;
    }

    /**
     * 设置下载目录
     * @param home
     */
    public void setHome(String home) {
        this.home = home;
    }

    /**
     * 获取下载文件类型（扩展名,如果包含在文件名内，则返回空）
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
     */
    public int getMode() {
        return mode;
    }

    /**
     * 设置校验模式，具体见DownloadOrder类
     * @param mode
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 获取下载状态，具体见DownloadOrder类
     * @return
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
     * 设置下载状态并刷新下载列表，具体见DownloadOrder类
     * 设置完成刷新一下下载列表。如下载成功后让下一个等待的任务开始执行
     * @param state
     */
    protected void setStateAndRefresh(int state) {
        this.state = state;
        DownloadList.refresh();
    }


    /**
     * 获取扩展信息
     * @return
     */
    public String getOther() {
        return other;
    }


    /**
     * 设置扩展信息
     * 如果是多个信息，建议使用分号进行分割
     * @param other
     */
    public void setOther(String other) {
        this.other = other;
    }




    @Override
    public String toString() {
        return "DownloadInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", home='" + home + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", md5='" + md5 + '\'' +
                ", size=" + size +
                ", other='" + other + '\'' +
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
        if(id <= 0){
            throw new IllegalParamsException("id", "不能为负或0");
        }
        if(home == null || home.equals("")){
            throw new IllegalParamsException("home", "不能为空");
        }
        if(url == null || url.equals("")){
            throw new IllegalParamsException("url", "不能为空");
        }
        if((mode & DownloadOrder.MODE_SIZE_START) == 1 && size <= 0){
            throw new IllegalParamsException("mode", "下载前校验文件大小必须提前设置文件大小");
        }
        if((mode & DownloadOrder.MODE_MD5_END) == 1 && (md5 == null || md5.equals(""))){
            throw new IllegalParamsException("mode", "校验文件MD5必须提前设置MD5");
        }
    }

}
