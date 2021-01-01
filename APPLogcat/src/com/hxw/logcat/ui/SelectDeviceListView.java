package com.hxw.logcat.ui;

import com.adb.process.Device;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Arrays;

/**
 * 选择要连接的设备
 */
public class SelectDeviceListView {

    private Stage stage = new Stage();

    public SelectDeviceListView(Device[] list,ISelectListener callback){

        AnchorPane root = new AnchorPane();
        ListView<Device> listView = new ListView<>();

        Label title = new Label("请选择要连接的设备:");
        title.setFont(Font.font(15));

        ObservableList<Device> data = FXCollections.observableArrayList();
        data.addAll(Arrays.asList(list));

        listView.setItems(data);

        AnchorPane.setTopAnchor(title,5.0);
        AnchorPane.setLeftAnchor(title,10.0);
        AnchorPane.setRightAnchor(title,10.0);
        AnchorPane.setTopAnchor(listView,30.0);
        AnchorPane.setLeftAnchor(listView,10.0);
        AnchorPane.setRightAnchor(listView,10.0);
        AnchorPane.setBottomAnchor(listView,10.0);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->{
            if(callback!=null){
                callback.onSelectItem(this,newValue);
            }
        });

        listView.setCellFactory(param -> new DeviceListCell());

        root.getChildren().addAll(title,listView);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("当前连接设备大于1");
        stage.setScene(new Scene(root));
        stage.setHeight(300);
        stage.setWidth(400);

    }

    /**
     * 显示窗口
     */
    public void show(){
        stage.show();
    }

    /**
     * 关闭窗口
     */
    public void close(){
        stage.close();
    }

    static class DeviceListCell extends ListCell<Device>{
        @Override
        protected void updateItem(Device item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null){
                this.setText("");
            }else{
                this.setText(item.deviceName);
            }
        }
    }

    public interface ISelectListener{
        public void onSelectItem(SelectDeviceListView view,Device t);
    }
}
