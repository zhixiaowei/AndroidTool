package com.hxw.logcat;

import com.adb.process.Device;
import com.hxw.logcat.data.ConfigBean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.hxw.logcat.ui.SelectDeviceListView;

import java.io.IOException;

public class APP extends Application implements Logcat.IMsgCallback{

    private TextArea logcatView = new TextArea();//日志输出
    private ConfigBean config = ConfigBean.getInstance();
    private Logcat logcat = new Logcat(this);
    private Device device = null;//多个设备连接时，记住用户最后一次选择连接的设备，避免每次修改参数都要选择设备

    private boolean isPause = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        initView(primaryStage);

        logcat.reset();//连接设备并输出日志
    }

    public void initView(Stage stage){

        AnchorPane root = new AnchorPane();

        initMenu(root);
        initLogCatView(root);

        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.DECORATED);//StageStyle.UTILITY在Window7显示不清晰
        stage.setTitle("日志小工具");
        stage.setHeight(600);
        stage.setWidth(800);
        stage.show();
    }

    /**
     * 初始化菜单栏
     * @param root
     */
    private void initMenu(AnchorPane root) {

        MenuBar menuBar = new MenuBar();

        menuBar.setPrefHeight(30);
        Menu codeType = new Menu("编码格式");
        Menu clean = new Menu();//清空
        Menu save = new Menu("保存");
        Menu filter = new Menu();//过滤
        Menu logcatLevel = new Menu("日志输出等级");
        Menu logcatOutput = new Menu("日志输出格式");
        Menu reconnect = new Menu();//重连设备
        Menu pause = new Menu();//重连设备

        initCodeTypeMenu(codeType);
        initCleanMenu(clean);
        initSaveMenu(save);
        initFilterMenu(filter);
        initLogcatLevelMenu(logcatLevel);
        initLogcatOutputMenu(logcatOutput);
        initReconnectMenu(reconnect);
        initPauseMenu(pause);

        AnchorPane.setTopAnchor(menuBar,2.0);
        AnchorPane.setRightAnchor(menuBar,10.0);
        AnchorPane.setLeftAnchor(menuBar,10.0);
        menuBar.getMenus().addAll(codeType,clean,save,filter,logcatLevel,logcatOutput,reconnect,pause);

        root.getChildren().add(menuBar);
    }

    private void initPauseMenu(Menu pause) {
        Label pauseLabel = new Label("暂停");
        pause.setGraphic(pauseLabel);
        pauseLabel.setOnMouseClicked(event -> {
            isPause = !isPause;
            pauseLabel.setText(isPause?"继续":"暂停");
        });
    }

    private void initReconnectMenu(Menu reconnect) {
        Label reconnectLabel = new Label("重连设备");
        reconnect.setGraphic(reconnectLabel);
        reconnectLabel.setOnMouseClicked(event -> {
            logcatView.clear();
            logcat.reset();
        });
    }

    private void initLogcatOutputMenu(Menu logcatOutput) {
        ToggleGroup logcatOutputGP = new ToggleGroup();
        RadioMenuItem raw = new RadioMenuItem("raw");
        RadioMenuItem process = new RadioMenuItem("process");
        RadioMenuItem tag = new RadioMenuItem("tag");
        RadioMenuItem brief = new RadioMenuItem("brief");
        RadioMenuItem time = new RadioMenuItem("time");
        RadioMenuItem all = new RadioMenuItem("long");
        logcatOutputGP.getToggles().addAll(raw,process,tag,brief,time,all);
        logcatOutput.getItems().addAll(raw,process,tag,brief,time,all);
        switch (config.getFormat()){
            case "raw":
                raw.setSelected(true);
                break;
            case "process":
                process.setSelected(true);
                break;
            case "brief":
                brief.setSelected(true);
                break;
            case "tag":
                tag.setSelected(true);
                break;
            case "long":
                all.setSelected(true);
                break;
            default:
                time.setSelected(true);
                break;
        }

        logcatOutputGP.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            String str = ((RadioMenuItem)newValue).getText();
            config.setFormat(str);
            logcat.reset(device);
        });
    }

    private void initLogcatLevelMenu(Menu logcatLevel) {
        ToggleGroup logcatLevelGp = new ToggleGroup();
        RadioMenuItem v = new RadioMenuItem("v：输出全部日志");
        RadioMenuItem d = new RadioMenuItem("d: Debug");
        RadioMenuItem i = new RadioMenuItem("i: Info");
        RadioMenuItem w = new RadioMenuItem("w: Warning");
        RadioMenuItem e = new RadioMenuItem("e: Error");
        RadioMenuItem f = new RadioMenuItem("f: Fatal");
        RadioMenuItem s = new RadioMenuItem("s: 不输出任何日志");
        logcatLevelGp.getToggles().addAll(v,d,i,w,e,f,s);
        v.setSelected(true);

        switch (config.getLevel()){
            case "v":
                v.setSelected(true);
                break;
            case "d":
                d.setSelected(true);
                break;
            case "w":
                w.setSelected(true);
                break;
            case "e":
                e.setSelected(true);
                break;
            case "f":
                f.setSelected(true);
                break;
            case "s":
                s.setSelected(true);
                break;
            default:
                i.setSelected(true);
                break;
        }


        logcatLevel.getItems().addAll(v,d,i,w,e,f,s);

        logcatLevelGp.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            String level = ((RadioMenuItem)newValue).getText().substring(0,1);
            config.setLevel(level);
            logcat.reset(device);
        });
    }

    private void initFilterMenu(Menu filter) {
        Label filterLabel = new Label("过滤");
        filter.setGraphic(filterLabel);
        filterLabel.setOnMouseClicked(event -> {
            showFilterView();//进行日志的过滤
        });
    }

    private void initCleanMenu(Menu clean) {
        Label cleanLabel = new Label("清空");
        clean.setGraphic(cleanLabel);
        cleanLabel.setOnMouseClicked(event ->{ logcatView.clear();});//清空日志
    }

    private void initSaveMenu(Menu save) {
        CheckMenuItem isSave = new CheckMenuItem("是否保存日志");
        isSave.setSelected(config.getFileConfig().isSave());
        isSave.selectedProperty().addListener((observable, oldValue, newValue) ->{
            config.getFileConfig().setSave(newValue);
        });


        MenuItem openLogFile = new MenuItem("打开日志文件夹");
        openLogFile.setOnAction(event -> {
            try {
                Runtime.getRuntime().exec("explorer.exe "+config.getFileConfig().getLogPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        save.getItems().addAll(isSave,openLogFile);
    }

    /**
     * 编码菜单
     * @param codeType
     */
    private void initCodeTypeMenu(Menu codeType) {
        ToggleGroup codeTypeTG = new ToggleGroup();
        RadioMenuItem UTF_8 = new RadioMenuItem("UTF-8");
        RadioMenuItem GBK = new RadioMenuItem("GBK");
        codeTypeTG.getToggles().addAll(UTF_8,GBK);
        codeType.getItems().addAll(UTF_8,GBK);
        codeTypeTG.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (UTF_8.isSelected()){
                config.setCharset("utf-8");
            }else{
                config.setCharset("GBK");
            }
        });
        if ("GBK".equals(config.getCharset())) {
            GBK.setSelected(true);
        } else {
            UTF_8.setSelected(true);
        }
    }

    /**
     * 初始化日志显示控件
     * @param root
     */
    private void initLogCatView(AnchorPane root) {
        logcatView.setEditable(false);//不可编辑
        logcatView.setWrapText(true);//自动换行

        AnchorPane.setRightAnchor(logcatView,10.0);
        AnchorPane.setLeftAnchor(logcatView,10.0);
        AnchorPane.setBottomAnchor(logcatView,10.0);
        AnchorPane.setTopAnchor(logcatView,32.0);

        root.getChildren().add(logcatView);
    }

    /**
     * 设置页面
     */
    private void showFilterView() {

        Stage filterView = new Stage();
        filterView.setWidth(400);
        filterView.setHeight(400);

        TextField packageText = new TextField();
        packageText.setPromptText("包名过滤");
        packageText.setText(config.getPackageName());
        packageText.setFocusTraversable(false);

        TextField keyText = new TextField();
        keyText.setPromptText("关键字过滤");
        keyText.setText(config.getGrep());
        keyText.setFocusTraversable(false);

        TextField TAGText = new TextField();
        TAGText.setPromptText("TAG 过滤");
        TAGText.setText(config.getTAG().equals("*")?"":config.getTAG());
        TAGText.setFocusTraversable(false);

        Button confirmBtn = new Button("应用");
        confirmBtn.setOnMouseClicked(event -> {
            config.setGrep(keyText.getText() == null?"":keyText.getText());
            config.setPackageName(packageText.getText() == null?"":packageText.getText());
            config.setTAG(TAGText.getText() == null?"*":TAGText.getText());
            logcat.reset(device);
            filterView.close();
        });

        AnchorPane group = new AnchorPane();
        AnchorPane.setRightAnchor(packageText,10.0);
        AnchorPane.setLeftAnchor(packageText,10.0);
        AnchorPane.setTopAnchor(packageText,10.0);

        AnchorPane.setRightAnchor(keyText,10.0);
        AnchorPane.setLeftAnchor(keyText,10.0);
        AnchorPane.setTopAnchor(keyText,50.0);

        AnchorPane.setRightAnchor(TAGText,10.0);
        AnchorPane.setLeftAnchor(TAGText,10.0);
        AnchorPane.setTopAnchor(TAGText,90.0);

        AnchorPane.setRightAnchor(confirmBtn,50.0);
        AnchorPane.setLeftAnchor(confirmBtn,50.0);
        AnchorPane.setTopAnchor(confirmBtn,120.0);

        group.getChildren().addAll(packageText,keyText,TAGText,confirmBtn);

        filterView.initModality(Modality.APPLICATION_MODAL);
        filterView.setScene(new Scene(group));
        filterView.setTitle("日志过滤");
        filterView.setHeight(200);
        filterView.setWidth(300);
        filterView.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(1);//UI结束的同时退出进程
    }

    @Override
    public void onReplyLine(String msg) {

        if (isPause){
            return;
        }

        Platform.runLater(() -> {
            if (logcatView.getLength() > 9999){
               logcatView.deleteText(0,1000);
            }

            logcatView.appendText(msg);
        });
    }

    @Override
    public void onChoiceDevice(Device[] devices) {
        //当连接的设备大于1，需要用户选择

        Platform.runLater(() -> {

            SelectDeviceListView selectView = new SelectDeviceListView(devices, (view, t) -> {
                this.device = t;
                logcatView.clear();
                logcat.reset(t);
                view.close();//选择设备重置日志并关闭选择列表
            });
            selectView.show();
        });
    }

    public static void main(String[] args) {
        launch();
    }


}
