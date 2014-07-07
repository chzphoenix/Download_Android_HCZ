package com.huichongzi.download_android.download;

/**
 * 下载信息bean类
 * Created by cuihz on 2014/7/3.
 */
public class DownloadInfo {

    private String id;
	private String name;
    private String home;
    private String type;
	private String url;
	private String md5;
	private long size;
    private int group;
    private int mode;
    private int state;
    private String other;


    /**
     * 获取下载唯一标示
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 设置下载唯一标示
     * @param id
     */
    public void setId(String id) {
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
        return home + "/" + name + "." + type;
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
     * 获取下载文件类型（扩展名）
     * @return
     */
    public String getType() {
        return type;
    }


    /**
     * 设置下载文件类型（扩展名）
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
    public int getGroup() {
        return group;
    }

    /**
     * 设置下载组，用于进行批量暂停等操作。如想新建一个下载组，则调用Downloader.getNewGroup()来获取一个新的不重复下载组id，在调用此方法。
     * @param group
     */
    public void setGroup(int group) {
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
    public void setStateAndRefresh(int state) {
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
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }


}
