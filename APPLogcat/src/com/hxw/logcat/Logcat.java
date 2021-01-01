package com.hxw.logcat;

import com.adb.process.ACtrl;
import com.adb.process.ADBCtrl;
import com.adb.process.Device;
import com.adb.process.android.logcat.AndroidLogcat;
import com.adb.process.android.logcat.LogcatConfig;
import com.adb.process.android.logcat.LogcatConfigBuilder;
import com.hxw.baselib.utils.FileUtils;
import com.hxw.logcat.data.ConfigBean;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logcat{

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private IMsgCallback callback;
    private ConfigBean bean = ConfigBean.getInstance();

    private Process process;//ADB执行日志的Process
    private String savePrefix = bean.getFileConfig().getPath()+ File.separator+sdf.format(new Date());//保存路径
    private ADBCtrl adbCtrl = new ADBCtrl();

    public Logcat(IMsgCallback callback){
        this.callback = callback;
    }

    public void reset(){
        new Thread(() -> startLog(null)).start();
    }

    public void reset(Device device){
        new Thread(() -> {
            startLog(device == null?null:device.deviceName);
        }).start();
    }


    private void startLog(String deviceName){
        if (process != null){
            process.destroy();
        }

        Device[] listDevice = adbCtrl.listDevices();
        Device device = null;//准备连接的设备

        if (listDevice == null||listDevice.length == 0){
            callback.onReplyLine("设备未连接");
            return;
        }else if (listDevice.length > 1){
            if (deviceName == null){
                //连接数大于1且用户为选择，需要用户选择连接那台设备
                this.callback.onChoiceDevice(listDevice);
                return;
            }else{
                //查看用户选择连接的设备是否还连接着
                boolean isFindDevice = false;
                for (Device d:listDevice){
                    if (d.deviceName.equals(deviceName)){
                        isFindDevice = true;
                        device = d;
                        break;
                    }
                }

                if (!isFindDevice){
                    //未找到要连接的设备，要用户重新选择
                    this.callback.onChoiceDevice(listDevice);
                    return;
                }
            }
        }else{
            //连接数等于1，直接连接
            device = listDevice[0];
        }

        if (!device.deviceName.trim().isEmpty()){

            device.isPrintCmd(true);

            String charset = bean.getCharset();
            device.setCharset(charset);

            LogcatConfig config = new LogcatConfigBuilder()
                    .filter(bean.getGrep())
                    .filterPackageName(bean.getPackageName())
                    .filterLevel(bean.getLevel())
                    .filterTAG(bean.getTAG())
                    .showFormat(bean.getFormat())
                    .build();

            AndroidLogcat logcat = device.managerOfLogcat();

            Logcat.this.callback.onReplyLine(config.cmd+"\r\n");

            try {


                String path = savePrefix+"_"+replaceSpecStr(device.deviceName)+".txt";//日志保存地址
                Logcat.this.callback.onReplyLine("日志保存路径："+path+"\r\n");
                FileOutputStream writer = new FileOutputStream(path,true);

                ACtrl.IExecCallback callback = new ACtrl.IExecCallback(){

                    @Override
                    public void onCreatedProcess(Process process) {
                        Logcat.this.process = process;
                    }

                    @Override
                    public void onReplyLine(String s) {

                        if (bean.getFileConfig().isSave()){
                            try {
                                writer.write(s.getBytes(charset));
                                writer.write("\r\n".getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Logcat.this.callback.onReplyLine(s+"\r\n");
                    }

                    @Override
                    public void onErrorLine(String s) {
                        Logcat.this.callback.onReplyLine(s+"\r\n");
                        FileUtils.close(writer);
                    }
                };
                logcat.listenLogcat(config,callback,false);
            } catch (IOException e) {
                callback.onReplyLine(e.getMessage() == null?"执行ADB指令失败！":e.getMessage());
                e.printStackTrace();
            }
        }else{
            callback.onReplyLine("获取设备名失败");
        }
    }


    /**
     * 正则替换所有特殊字符
     * @param orgStr
     * @return
     */
    public static String replaceSpecStr(String orgStr){
        String lastStr = "";

        if (null!=orgStr&&!"".equals(orgStr.trim())) {
            String regEx="[\\s~·`!！@#￥$%^……&*（()）\\-——\\-_=+【\\[\\]】｛{}｝\\|、\\\\；;：:‘'“”\"，,《<。.》>、/？?]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(orgStr);

            lastStr = m.replaceAll("");
        }


        return lastStr.isEmpty()?"未知":lastStr;
    }


    public interface IMsgCallback{
        void onReplyLine(String msg);
        void onChoiceDevice(Device[] devices);
    }
}
