package com.hxw.logcat.data;

import com.google.gson.Gson;
import com.hxw.baselib.utils.FileUtils;
import com.hxw.baselib.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConfigBean {

    private String PackageName = "";
    private String Grep = "";
    private String Format = "time";
    private String Level = "v";
    private String TAG = "";
    private String Charset = "UTF-8";
    private FileConfig fileConfig = new FileConfig();

    public ConfigBean() {}

    private static ConfigBean build(){
        String path = new File(FileUtils.getLocalPath(ConfigBean.class,"UTF-8")).getParentFile().getAbsolutePath()+"\\config.json";
        File configFile = new File(path);

        if (!configFile.exists()||configFile.length() <= 0){
            createConfigFile(path);
        }else{
            System.out.println("配置文件加载完毕");
        }

        String text = FileUtils.readText(path);
        System.out.println(text);

        return new Gson().fromJson(text,ConfigBean.class);
    }


    private static void createConfigFile(String path) {
        System.out.println("创建配置文件:"+path);
        try {
            FileUtils.createNewFile(path);
            ConfigBean bean = new ConfigBean();
            String msg = new Gson().toJson(bean);
            saveText(path,msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存日志文本
     * @param text
     */
    private static void saveText(String path,String text) {
        try {
            FileOutputStream fos = new FileOutputStream(path, false);
            //将要写入的字符串转换为byte数组
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConfigBean getInstance() {
        return ConfigBean.SingletonHolder.sInstance;
    }

    private static class SingletonHolder{
        private static final ConfigBean sInstance = ConfigBean.build();
    }

    public FileConfig getFileConfig() {

        if (fileConfig == null){
            fileConfig = new FileConfig();
        }
        return fileConfig;
    }

    public void setFileConfig(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    public String getCharset() {
        return Charset;
    }

    public void setCharset(String charset) {
        if (!charset.equals(this.Charset)){
            saveConfig(this);
        }
        Charset = charset;
    }


    public void setPackageName(String PackageName) {


        if (!PackageName.equals(this.PackageName)){
            this.PackageName = PackageName;
            saveConfig(this);
        }

    }
    public String getPackageName() {

        if (StringUtils.isEmpty(PackageName)){
            return null;
        }

        return PackageName;
    }

    public void setGrep(String Grep) {
        if (!Grep.equals(this.Grep)){
            this.Grep = Grep;
            saveConfig(this);
        }

    }
    public String getGrep() {
        if (StringUtils.isEmpty(Grep)){
            return null;
        }
        return Grep;
    }

    public void setFormat(String Format) {
        if (!Format.equals(this.Format)){
            this.Format = Format;
            saveConfig(this);
        }


    }
    public String getFormat() {

        if (StringUtils.isEmpty(Format)){
            return "time";
        }

        return Format;
    }

    public void setLevel(String Level) {
        if (!Level.equals(this.Level)){
            this.Level = Level;
            saveConfig(this);
        }

    }
    public String getLevel() {

        if (Level == null||Level.trim().isEmpty()){
            return "v";
        }

        return Level;
    }

    public void setTAG(String TAG) {
        if (!TAG.equals(this.TAG)){
            this.TAG = TAG;
            saveConfig(this);
        }
    }

    public String getTAG() {
        if (StringUtils.isEmpty(TAG)){
            return "*";
        }

        return TAG;
    }

    public static void saveConfig(ConfigBean bean){
        System.out.println("保存配置文件");
        try {
            String path = new File(FileUtils.getLocalPath(ConfigBean.class,"UTF-8")).getParentFile().getAbsolutePath()+"\\config.json";
            FileUtils.createNewFile(path);
            String msg = new Gson().toJson(bean);
            saveText(path,msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class FileConfig {
        private String path = "";
        private volatile boolean isSave = false;

        public String getPath() {
            if (StringUtils.isEmpty(path)){
                return getLogPath();
            }

            return path;
        }

        public void setPath(String path) {
            if (this.path.equals(path)){
                this.path = path;
                saveConfig(ConfigBean.getInstance());
            }

        }

        public void setSave(boolean save) {
            if (isSave != save){
                isSave = save;

                saveConfig(ConfigBean.getInstance());
            }
        }

        public boolean isSave() {
            return isSave;
        }

        public String getLogPath(){

            if (path!=null&&!path.trim().isEmpty()){
                return path;
            }

            String jarPath = FileUtils.getLocalPath(getClass(),"UTF-8");
            String logPath = new File(jarPath).getParent()+ File.separator+"LOG";
            File logFile = new File(logPath);

            if (!logFile.exists()){
                logFile.mkdir();
            }

            return logPath;
        }
    }

}
