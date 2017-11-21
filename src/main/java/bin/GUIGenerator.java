package bin;

import classroom.Component;
import classroom.Project;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.ComponentOperator;
import utils.FileOperator;
import view.OptionListController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class GUIGenerator extends Application {
    private DataGenerator dataGenerator = new DataGenerator("Config.xml");
    private FileOperator fileOpener = new FileOperator();
    private ComponentOperator componentOperator = new ComponentOperator();

    private Stage primaryStage;
    private BorderPane rootLayout;
    private int maxCountComponents;
    private HashSet<String> openComponentLists = new HashSet<>();

    public GUIGenerator() {
    }

    public ComponentOperator getComponentOperator() {
        return componentOperator;
    }

    public FileOperator getFileOpener() {
        return fileOpener;
    }

    @Override
    public void start(Stage primaryStage) { // Формирование корневого макета
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("kitUP");
        this.primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
        this.primaryStage.setResizable(false);

        initRootLayout(); // Корневой макет
    }

    /**
     * Корневой макет
     */
    private void initRootLayout() {
        // Загрузка корневого макета
        rootLayout = new BorderPane();
        showProjectOverview(); // Проекты

        // Отображение сцены, содержащую корневой макет
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add("/MainStylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Проекты
     */
    private void showProjectOverview() {
        // Инициализация
        TabPane projectsOverview = new TabPane();

        // Настройка
        projectsOverview.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        projectsOverview.setMinWidth(200);

        // Загрузка данных по проектам
        projectsOverview.getTabs().addAll(getProjectList());

        // Отправить макет проектов в корневой макет
        rootLayout.setMinSize(200, 60 + 37 * maxCountComponents);
        rootLayout.setCenter(projectsOverview);
    }

    /**
     * Возвращает список проектов
     *
     * @return projectList
     */
    private ArrayList<Tab> getProjectList() { // Формирование закладок
        ArrayList<Tab> projectList = new ArrayList<>();

        for (int i = 0; i < dataGenerator.getProjects().size(); i++) {
            // Инициализация
            Tab tab = new Tab();
            AnchorPane tabRootPane = new AnchorPane();
            VBox leftVBox = new VBox();
            VBox rightVBox = new VBox();

            // Настройка
            tab.setText(dataGenerator.getProjects().get(i).getProjectName());

            leftVBox.getChildren().addAll(getComponentList(dataGenerator.getProjects().get(i), dataGenerator.getProjects().get(i).getComponents()));
            leftVBox.setSpacing(10);
            AnchorPane.setLeftAnchor(leftVBox, 20.0);
            AnchorPane.setTopAnchor(leftVBox, 20.0);
            AnchorPane.setRightAnchor(leftVBox, 7.0);

            rightVBox.getChildren().addAll(getComponentOptionList(dataGenerator.getProjects().get(i), dataGenerator.getProjects().get(i).getComponents()));
            rightVBox.setMaxWidth(24);
            rightVBox.setSpacing(10);
            AnchorPane.setRightAnchor(rightVBox, 7.0);
            AnchorPane.setTopAnchor(rightVBox, 20.0);

            // Отправить
            tabRootPane.getChildren().addAll(leftVBox, rightVBox);
            tab.setContent(tabRootPane);

            // Добавление проекта в список
            projectList.add(tab);
        }
        return projectList;
    }

    /**
     * Возвращает список компонент для проекта
     *
     * @return componentList
     */
    private ArrayList<Button> getComponentList(Project project, ArrayList<Component> components) { // Формирование кнопок
        ArrayList<Button> componentList = new ArrayList<>();

        if (maxCountComponents < components.size()) {
            maxCountComponents = components.size();
        }

        for (Component component : components) {
            Button button = new Button();
            button.setText(component.getComponentName());
            button.setMinWidth(150);
            button.setOnAction(
                    (e) -> {
                        button.setDisable(true);

                        Service backqroundThread = new Service<Void>() {
                            protected Task<Void> createTask() {
                                return new Task<>() {
                                    protected Void call() {
                                        componentOperator.updateComponent(project.getServerName(), component.getServiceName(), component.getLastVersionDirName(), component.getComponentDirName());
                                        return null;
                                    }
                                };
                            }
                        };

                        backqroundThread.setOnSucceeded(event -> button.setDisable(false));
                        backqroundThread.restart();
                    }
            );
            componentList.add(button);
        }
        return componentList;
    }

    /**
     * Возвращает список дополнительных опций для компоненты
     *
     * @return componentOptionList
     */
    private ArrayList<Button> getComponentOptionList(Project project, ArrayList<Component> components) {
        ArrayList<Button> componentOptionList = new ArrayList<>();

        for (Component component : components) {
            Button moreOptionsButton = new Button();
            moreOptionsButton.setText("");
            moreOptionsButton.setPrefWidth(14);
            moreOptionsButton.setOnAction(
                    (ActionEvent event) -> {
                        if (!openComponentLists.contains(project.getProjectName() + "." + component.getComponentName())) {
                            openComponentLists.add(project.getProjectName() + "." + component.getComponentName());
                            ShowOptionList(project, component);
                        }
                    });

            componentOptionList.add(moreOptionsButton);
        }
        return componentOptionList;
    }

    /**
     * Открыть диалоговое окно дополнительных опций компоненты
     */
    private void ShowOptionList(Project project, Component component) {
        try {
            // Загрузить fxml-файл для создания новой сцены для окна
            FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("OptionList.fxml"));
            VBox page = loader.load();
            OptionListController optionListController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setAlwaysOnTop(true);
            dialogStage.initModality(Modality.NONE);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(null);
            dialogStage.setResizable(false);

            dialogStage.focusedProperty().addListener((ov, t, t1) -> optionListController.windowFocused(t1));

            dialogStage.setOnHiding(e -> {
                openComponentLists.remove(project.getProjectName() + "." + component.getComponentName());
                optionListController.threadIsDead(); //Окно закрыто и все порожденные потоки останавливаются
            });

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передача настройки в контроллер
            optionListController.setGuiGenerator(this, dialogStage);
            optionListController.setComponentData(project, component);

            dialogStage.show();
        } catch (IOException e) {
            System.err.println("Could not load OptionList :" + e.toString());
        }
    }

    public void launchGUI(String[] args) {
        launch(args);
    }
}