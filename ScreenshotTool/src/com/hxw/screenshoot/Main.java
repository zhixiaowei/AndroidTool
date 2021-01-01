package com.hxw.screenshoot;

import com.adb.process.ADBCtrl;
import com.adb.process.Device;
import com.adb.process.android.AndroidFile;
import sun.text.CodePointIterator;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Device device = new ADBCtrl().firstDevice();
        if (device!=null){
            device.isPrintCmd(true);
            String path = StringUtils.decode(new File(JarUtils.getJarPath()).getParentFile().getAbsolutePath(),"UTF-8");

            System.out.println(path);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = sdf.format(new Date());

            AndroidFile file = device.managerOfFile();
            String list = file.listDir(file.getExternalStorageDirectoryPath());
            System.out.println(new String(list.getBytes(), StandardCharsets.UTF_8));

            device.screenshot2WindowFile(path+"/"+date+".png");
        }else{
            System.out.println("未找到设备");
        }

    }
}
