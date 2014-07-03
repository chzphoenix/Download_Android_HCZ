package com.huichongzi.download_android.download;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.*;


class UnFinishedConfFile {
	
	private Map<String, String> conf = new HashMap<String, String>();
	private File file;
	private final static String SPLIT = "/ifeng/";
    protected static final String Unfinished_Conf_Sign = ".dz";

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
			e.printStackTrace();
		}
	}

    protected boolean isLock(){
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			FileLock lock = raf.getChannel().tryLock();
			if(lock.isValid()){
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		
	}

    protected String getValue(String key){
		return conf.get(key);
	}

    protected void put(String key, String value){
		conf.put(key, value);
	}

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
			e.printStackTrace();
		}
		
	}

    protected void delete(){
		if(file.exists() && file.isFile()){
			file.delete();
		}
	}

    protected boolean isConfNull(){
		if(conf.size() == 0){
			return true;
		}
		return false;
	}
	
	
}
