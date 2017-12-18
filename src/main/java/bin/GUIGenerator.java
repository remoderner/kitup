package bin;

import classroom.Component;
import classroom.KitupConfig;
import classroom.Project;
import classroom.Server;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.ComponentOperator;
import utils.FileOperator;
import utils.ServiceOperator;
import view.OptionListController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class GUIGenerator extends Application {
    //private final String HOST = "localhost";
    //private final int PORT = 3301;
    private KitupConfig kitupConfig = KitupConfig.getKitupConfig();
    private FileOperator fileOpener = new FileOperator();
    private ComponentOperator componentOperator = new ComponentOperator();
    private ServiceOperator serviceOperator = new ServiceOperator();
    private Stage rootStage;

    /**
     * Client-server features
     *
    private EventLoopGroup workerGroup;*/
  
    private HashSet<String> openComponentLists = new HashSet<>();

    public GUIGenerator() {
    }

    public ServiceOperator getServiceOperator() {
        return serviceOperator;
    }

    public FileOperator getFileOpener() {
        return fileOpener;
    }

    @Override
    public void start(Stage primaryStage) {
        showRoot(); // Show root stage
    }

    /**
     * ROOT STAGE
     */
    private void showRoot() {
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("Root.fxml"));
        VBox page = null;
        try {
            page = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OptionListController optionListController = loader.getController();

        rootStage = new Stage();
        rootStage.initStyle(StageStyle.TRANSPARENT);
        rootStage.setResizable(false);
        rootStage.setTitle("kitUP" + " / " + "2.0.3");
        rootStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));

        rootStage.focusedProperty().addListener((ov, t, t1) -> optionListController.windowFocused(t1));

        assert page != null;
        Scene scene = new Scene(page);
        scene.getStylesheets().add("/NewMainRoot.css");
        rootStage.setScene(scene);

        // Передача настройки в контроллер
        optionListController.setGuiGenerator(this, rootStage);
        optionListController.setRootData(getProjectsList());

        rootStage.show();
    }

    /**
     * Возвращает список проектов
     *
     * @return projectList
     */
    private ArrayList<Tab> getProjectsList() { // Формирование закладок
        ArrayList<Tab> projectList = new ArrayList<>();

        for (int i = 0; i < kitupConfig.getProjects().size(); i++) {
            Tab tab = new Tab();
            AnchorPane tabRootPane = new AnchorPane();
            VBox leftVBox = new VBox();
            VBox rightVBox = new VBox();

            tab.setText(kitupConfig.getProjects().get(i).getProjectName());

            leftVBox.getChildren().addAll(getServerOptionList(kitupConfig.getProjects().get(i), kitupConfig.getProjects().get(i).getServers()));
            leftVBox.getChildren().addAll(getComponentOptionList(kitupConfig.getProjects().get(i), kitupConfig.getProjects().get(i).getComponents()));
            leftVBox.setSpacing(0);
            AnchorPane.setLeftAnchor(leftVBox, 10.0);
            AnchorPane.setTopAnchor(leftVBox, 0.0);
            AnchorPane.setRightAnchor(leftVBox, 10.0);
            AnchorPane.setBottomAnchor(leftVBox, 10.0);

            //rightVBox.getChildren().addAll(getComponentOptionList(dataGenerator.getProjects().get(i), dataGenerator.getProjects().get(i).getComponents()));
            //rightVBox.setMaxWidth(30);
            //rightVBox.setSpacing(10);
            //AnchorPane.setRightAnchor(rightVBox, 25.0);
            //AnchorPane.setTopAnchor(rightVBox, 25.0);
            //AnchorPane.setBottomAnchor(rightVBox, 25.0);

            tabRootPane.getChildren().addAll(leftVBox);
            tab.setContent(tabRootPane);

            projectList.add(tab);
        }
        return projectList;
    }

    /**
     * Возвращает список серверов для проекта
     *
     * @return serverList
     */
    private ArrayList<Button> getServerList(Project project, ArrayList<Server> servers) { // Формирование кнопок серверов
        ArrayList<Button> serverList = new ArrayList<>();

        for (Server server : servers) {
            Button button = new Button();
            button.setText(server.getServerName());
            button.setMinWidth(150);
            button.setOnAction(
                    (e) -> {
                        button.setDisable(true);

                        Service backqroundThread = new Service<Void>() {
                            protected Task<Void> createTask() {
                                return new Task<>() {
                                    protected Void call() {
                                        //componentOperator.updateServer(project.getServerName(), server.getServiceNameQortes(), server.getServiceNameQortesDB(), server.getLastVersionDirName(), server.getServerDirName());
                                        return null;
                                    }
                                };
                            }
                        };

                        backqroundThread.setOnSucceeded(event -> button.setDisable(false));
                        backqroundThread.restart();
                    }
            );
            serverList.add(button);
        }
        return serverList;
    }

    /**
     * Возвращает список компонент для проекта
     *
     * @return componentList
     */
    private ArrayList<Button> getComponentList(Project project, ArrayList<Component> components) { // Формирование кнопок компонент
        ArrayList<Button> componentList = new ArrayList<>();

        for (Component component : components) {
            Button button = new Button();
            button.setText(component.getComponentName());
            button.setMinWidth(180);
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
     * @return serverOptionList
     */
    private ArrayList<Button> getServerOptionList(Project project, ArrayList<Server> servers) {
        ArrayList<Button> componentOptionList = new ArrayList<>();

        for (Server server : servers) {
            Button moreOptionsButton = new Button();
            moreOptionsButton.setText(server.getServerName());
            moreOptionsButton.setStyle("-fx-font-size: 13px");
            moreOptionsButton.setMinWidth(180);
            moreOptionsButton.setOnAction(
                    (ActionEvent event) -> {
                        if (!openComponentLists.contains(project.getProjectName() + "." + server.getServerName())) {
                            openComponentLists.add(project.getProjectName() + "." + server.getServerName());
                            ShowServerOptionList(project, server);
                        }
                    });

            componentOptionList.add(moreOptionsButton);
        }
        return componentOptionList;
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
            moreOptionsButton.setText(component.getComponentName());
            moreOptionsButton.setStyle("-fx-font-size: 13px");
            moreOptionsButton.setMinWidth(180);
            moreOptionsButton.setOnAction(
                    (ActionEvent event) -> {
                        if (!openComponentLists.contains(project.getProjectName() + "." + component.getComponentName())) {
                            openComponentLists.add(project.getProjectName() + "." + component.getComponentName());
                            ShowComponentOptionList(project, component);
                        }
                    });

            componentOptionList.add(moreOptionsButton);
        }
        return componentOptionList;
    }

    /*private void connectToServer(Project project, Component component) {
        workerGroup = new NioEventLoopGroup();

        Task<Channel> task = new Task<>() {

            @Override
            protected Channel call() throws Exception {

                try {
                    Bootstrap b = new Bootstrap();
                    b
                            .group(workerGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    System.out.println("initChannel");
                                    final ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(
                                            new StringDecoder(),
                                            new ClientHandler(project.getProjectName(), component.getComponentName(), "vladiv"));
                                }
                            });

                    System.out.println("Start client to host: " + HOST + ":" + PORT);
                    ChannelFuture f = b.connect(HOST, PORT).sync();
                    f.channel().closeFuture().sync();
                } finally {
                    //workerGroup.shutdownGracefully();
                }

                return null;
            }
        };

        new Thread(task).start();
    }*/

    /**
     * Открыть диалоговое окно дополнительных опций сервера
     */
    private void ShowServerOptionList(Project project, Server server) {
        try {
            // Загрузить fxml-файл для создания новой сцены для окна
            FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("ServerOptionList.fxml"));
            VBox page = loader.load();
            OptionListController optionListController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setAlwaysOnTop(true);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initOwner(null);
            dialogStage.setResizable(false);
            dialogStage.setTitle(project.getProjectName() + " / " + server.getServerName());
            dialogStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
            dialogStage.setX(rootStage.getX() + rootStage.getWidth());
            dialogStage.setY(rootStage.getY());

            dialogStage.focusedProperty().addListener((ov, t, t1) -> optionListController.windowFocused(t1));

            dialogStage.setOnHiding(e -> {
                openComponentLists.remove(project.getProjectName() + "." + server.getServerName());
                optionListController.threadIsDead(); //Окно закрыто и все порожденные сервисы мониторинга останавливаются
                //workerGroup.shutdownGracefully(); //Закрывается подключение к серверу
            });

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передача настройки в контроллер
            optionListController.setGuiGenerator(this, dialogStage);
            optionListController.setServerData(project, server);
            //connectToServer(project, component); //Открывается подключение к серверу

            dialogStage.show();
        } catch (IOException e) {
            System.err.println("Could not load OptionList :" + e.toString());
        }
    }

    /**
     * Открыть диалоговое окно дополнительных опций компоненты
     */
    private void ShowComponentOptionList(Project project, Component component) {
        // Загрузить fxml-файл для создания новой сцены для окна
        FXMLLoader loaderOptionList = new FXMLLoader(this.getClass().getClassLoader().getResource("OptionList.fxml"));
        VBox page = null;
        try {
            page = loaderOptionList.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OptionListController optionListController = loaderOptionList.getController();

        Stage dialogStage = new Stage();
        dialogStage.setAlwaysOnTop(true);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initOwner(null);
        dialogStage.setResizable(false);
        dialogStage.setTitle(project.getProjectName() + " / " + component.getComponentName());
        dialogStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
        dialogStage.setX(rootStage.getX() + rootStage.getWidth());
        dialogStage.setY(rootStage.getY());

        dialogStage.focusedProperty().addListener((ov, t, t1) -> optionListController.windowFocused(t1));

        dialogStage.setOnHiding(e -> {
            openComponentLists.remove(project.getProjectName() + "." + component.getComponentName());
            optionListController.threadIsDead(); //Окно закрыто и все порожденные сервисы мониторинга останавливаются
            //workerGroup.shutdownGracefully(); //Закрывается подключение к серверу
        });

        assert page != null;
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        // Передача настройки в контроллер
        optionListController.setGuiGenerator(this, dialogStage);
        optionListController.setComponentData(project, component);
        //connectToServer(project, component); //Открывается подключение к серверу

        dialogStage.show();
    }

    public void launchGUI(String[] args) {
        launch(args);
    }
}