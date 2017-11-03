package bin;

import classroom.Component;
import classroom.Project;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.ComponentOperator;
import utils.Threader;

import java.io.IOException;
import java.util.ArrayList;

public class GUIGenerator extends Application {
    private DataGenerator dataGenerator = new DataGenerator("Config.xml");
    private ComponentOperator componentOperator = new ComponentOperator();
    private Thread mSecondThread;
    private int maxCountComponents;

    public void launchGUI(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException { //Формирование корневого макета
        //Настройки корневого макета
        primaryStage.setTitle("kitUP");
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
        primaryStage.setResizable(false);

        //Панель закладок
        TabPane tabPane = new TabPane();

        //Настрока панели закладок
        tabPane.getTabs().addAll(getTabList()); //Отобразить закладку
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(60 + 35 * maxCountComponents);
        tabPane.setMinWidth(Double.MIN_VALUE);

        //Сцена
        Scene scene = new Scene(tabPane);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private ArrayList<Tab> getTabList() { //Формирование закладок
        ArrayList<Tab> tabList = new ArrayList<>();

        for (int i = 0; i < dataGenerator.getProjects().size(); i++) {
            AnchorPane root = new AnchorPane(); //Создать главную панель
            VBox vBox = new VBox(10); //Создать панель для кнопок в центре

            root.getChildren().add(vBox); //Добавить панель vBox в центр главной панели

            vBox.setPadding(new Insets(20, 25, 10, 25));
            vBox.getChildren().addAll(getButtonList(dataGenerator.getProjects().get(i), dataGenerator.getProjects().get(i).getComponents())); //Отправить кнопки в панель
            vBox.setMaxWidth(200);

            Tab tab = new Tab();
            tab.setText(dataGenerator.getProjects().get(i).getProjectName());
            tab.setContent(root); //Отправить панель в закладку

            tabList.add(tab);
        }
        return tabList;
    }

    private ArrayList<Button> getButtonList(Project project, ArrayList<Component> components) { //Формирование кнопок
        ArrayList<Button> buttonList = new ArrayList<>();

        if (maxCountComponents < components.size()) {
            maxCountComponents = components.size();
        }

        for (Component component : components) {
            Button button = new Button();
            button.setText(component.getComponentName());
            button.setPrefWidth(150);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(
                    (ActionEvent b) -> {
                        button.setStyle("-fx-background-color: #00FA9A;");
                        button.setDisable(true);
                        mSecondThread = new Threader(project, component, componentOperator, button);
                        mSecondThread.start();
                    }
            );
            buttonList.add(button);
        }
        return buttonList;
    }
}