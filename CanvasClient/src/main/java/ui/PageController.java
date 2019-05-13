package ui;

import bl.PageBL;
import blservice.PageBLService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.svg.SVGGlyph;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import vo.ImageVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class PageController {

    private MainUIController mainUIController;

    private ArrayList<FXMLLoader> loaders = new ArrayList<>();
    private ArrayList<AnchorPane> cells = new ArrayList<>();

    private boolean isEdit = false;

    private ArrayList<ImageVO> myImages = new ArrayList<>();

    private PageBLService pageBLService = new PageBL();

    @FXML
    private JFXScrollPane pane;

//    @FXML
//    AnchorPane mainPane;

    @FXML
    private JFXButton editButton;

    private TilePane picList;

    @FXML
    private void initialize(){
        editButton.setText("\ue611");

        picList = new TilePane();
        picList.setMinHeight(600);
        picList.setPadding(new Insets(24));
        picList.setHgap(40);
        picList.setVgap(40);
        picList.setPrefColumns(4);
        picList.setPadding(new Insets(50, 50, 50, 50));
        pane.getStyleClass().add("mylistview");

        pane.setContent(picList);
//        pane.getMainHeader().setBackground(new Background(new BackgroundFill(Paint.valueOf("#87CEFA"),null,null)));

        //返回按钮初始化
        JFXButton button = new JFXButton("");
        SVGGlyph arrow = new SVGGlyph(0,
                "FULLSCREEN",
                "M402.746 877.254l-320-320c-24.994-24.992-24.994-65.516 0-90.51l320-320c24.994-24.992 65.516-24.992 90.51 0 24.994 24.994 "
                        + "24.994 65.516 0 90.51l-210.746 210.746h613.49c35.346 0 64 28.654 64 64s-28.654 64-64 64h-613.49l210.746 210.746c12.496 "
                        + "12.496 18.744 28.876 18.744 45.254s-6.248 32.758-18.744 45.254c-24.994 24.994-65.516 24.994-90.51 0z",
                Color.WHITE);
        arrow.setSize(20, 16);
        button.setGraphic(arrow);
        button.setRipplerFill(Color.WHITE);
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainUIController.showLogin();
            }
        });
        pane.getTopBar().getChildren().add(button);

        Label title = new Label("My Pictrue");
        pane.getBottomBar().getChildren().add(title);
        title.setStyle("-fx-text-fill:WHITE; -fx-font-size: 40;");
        JFXScrollPane.smoothScrolling((ScrollPane) pane.getChildren().get(0));

        StackPane.setMargin(title, new Insets(0, 0, 0, 80));
        StackPane.setAlignment(title, Pos.CENTER_LEFT);
        StackPane.setAlignment(button, Pos.CENTER_LEFT);
        StackPane.setMargin(button, new Insets(0, 0, 0, 20));

        showPicList();
    }

    //显示图片列表
    private void showPicList(){
        myImages = pageBLService.getAllPics();
        loaders.clear();
        cells.clear();
        picList.getChildren().clear();

        for (int i = 0; i < myImages.size(); i++){
            try {
                FXMLLoader picLoader = new FXMLLoader();
                picLoader.setLocation(getClass().getResource("/view/pageCell.fxml"));
                AnchorPane cell = picLoader.load();
                loaders.add(picLoader);
                cells.add(cell);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i =0; i < myImages.size(); i++){
            PageCellController pageCellController = loaders.get(i).getController();
            pageCellController.setPageController(this);
            pageCellController.setCellInfo(myImages.get(i));
            picList.getChildren().add(cells.get(i));
        }

        if(isEdit){
            isEdit = false;
            clickEditButton();
        }
    }

    public void setMainUIController(MainUIController mainUIController){
        this.mainUIController = mainUIController;
    }

    //点击最小化按钮
    @FXML
    private void clickMinusButton(){
        mainUIController.minimizeWindow();
    }

    //点击关闭按钮
    @FXML
    private void clickCloseButton(){
        mainUIController.closeWindow();
    }

    //点击编辑按钮
    @FXML
    private void clickEditButton(){
        if(isEdit){
            isEdit = false;
            editButton.setText("\ue611");
        }
        else{
            isEdit = true;
            editButton.setText("\uea16");
        }
        for(int i=0;i<loaders.size();i++){
            FXMLLoader loader = loaders.get(i);
            PageCellController pageCellController = loader.getController();
            pageCellController.setDeleteButton(isEdit);
        }
    }

    //点击删除按钮
    public void clickDeleteButton(String picName){
        Dialog alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("确定要删除该图片吗？");
        alert.setTitle("Canvas");
        Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null)
            cancelButton.setId("CancelButton");
        alert.getDialogPane().getStylesheets().add("/style/dialog.css");
        Optional result = alert.showAndWait();


        if (result.isPresent()){
            if (result.get() == ButtonType.OK) {
                pageBLService.deletePic(picName);
                showPicList();
            }
        }
    }

    //点击打开按钮
    public void clickOpenButton(String picName){
        mainUIController.showDrawBoard(picName);
    }
}
