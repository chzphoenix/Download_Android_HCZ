package com.huichongzi.download_android.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.*;

/**
 * 未完成下载的配置文件类
 * Created by cuihz on 2014/7/3.
 */
class UnFinishedConfFile {
    private static final Logger logger = LoggerFactory.getLogger(UnFinishedConfFile.class);
	private Map<String, String> conf = new HashMap<String, String>();
	private File file;
    //配置文件的分隔符
	private final static String SPLIT = "=";
    //配置文件的扩展名
    protected static final String Unfinished_Conf_Sign = ".dz";

    /**
     * 根据路径初始化配置
     * 1、文件存在，读取配置到map中
     * 2、文件不存在，创建文件
     * @param path 下载文件的路径
     */
    protected UnFinishedConfFile(String path){
		try {
			file = new File(path + Unfinished_Conf_Sign);
			if(!file.exists() || file.isDirectory()){
				file.getParentFile().mkdirs();
				return;
			}
			BufferedReader br = new BufferedReader(new FileReader(path + Unfinished_Conf_Sign));
			String line;
			while((line = br.readLine()) != null){
				String[] value = line.split(SPLIT);
				conf.put(value[0], value[1]);
			}
		} catch (Exception e) {
            logger.error("下载配置文件出错: {}", e.getMessage());
		}
	}


    /**
     * 判断文件是否被使用
     * @return boolean
     */
    protected boolean isLock(){
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileLock lock = raf.getChannel().tryLock();
			if(lock.isValid()){
				return false;
			}
			return true;
		} catch (Exception e) {
            logger.error("下载配置文件出错: {}", e.getMessage());
			return true;
		}
		
	}

    protected String getValue(String key){
		return conf.get(key);
	}

    protected void put(String key, String value){
		conf.put(key, value);
	}


    /**
     * 将配置map中的信息保存到文件中
     */
    protected void write(){
		StringBuffer sb = new StringBuffer();
		for(String key : conf.keySet()){
			sb.append(key + SPLIT + conf.get(key) + "\n");
		}
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
            logger.error("下载配置文件出错: {}", e.getMessage());
		}
		
	}

    protected void delete(){
		if(file.exists()){
			file.delete();
		}
	}

    /**
     * 判断配置是否存在
     * @return boolean
     */
    protected boolean isConfNull(){
		if(conf.size() == 0){
			return true;
		}
		return false;
	}
	
	
}
