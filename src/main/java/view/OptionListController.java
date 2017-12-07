package view;

import bin.GUIGenerator;
import classroom.Component;
import classroom.Project;
import classroom.Server;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ComponentOperator;
import utils.ServerOperator;
import utils.ServiceOperator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class OptionListController {
    private static final Logger logger = LogManager.getLogger(OptionListController.class);

    @FXML
    VBox rootVBox;

    @FXML
    VBox contentVBox;

    @FXML
    HBox contentHBox;

    @FXML
    TabPane projectsOverview;

    @FXML
    HBox titleHBox;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Label titleLabel;

    @FXML
    private Button update;
    private Button updateMin = new Button();

    @FXML
    private Button restart;
    private Button restartMin = new Button();

    @FXML
    private Button start;
    private Button startMin = new Button();

    @FXML
    private Button stop;
    private Button stopMin = new Button();

    @FXML
    private Button ini;

    @FXML
    private Button log;

    @FXML
    private Button dir;

    @FXML
    private VBox rollbackDates;

    @FXML
    private Button getRollbackDates;

    @FXML
    private Text componentVersion;

    private String mainCSS = "/MainStylesheet.css";
    private String buttonGoodCSS = "/ButtonGood.css";
    private String buttonWarningCSS = "/ButtonWarning.css";
    private String buttonStartCSS = "/ButtonStart.css";
    private String buttonStopCSS = "/ButtonStop.css";

    private String pathServer;
    private String pathSales;
    private String serviceName;
    private String pathLastVersion;
    private String pathComponent;
    private String componentName;
    private String lastComponentVersion;
    private String serviceNameQortes;
    private String serviceNameQortesDB;
    private String exeNameQortesDB;
    private String exeNameQortes;
    private String pathServerDir;
    private String serverName;

    private Boolean threadIsAlive = true;
    private Boolean isMinimized = false;
    private Boolean isEntered = false;
    private Boolean isStoped = false;

    private int dialogStageDefaultHeight = 330;
    private int dialogStageHeight = dialogStageDefaultHeight;

    private Stage dialogStage;
    private GUIGenerator guiGenerator;
    private ComponentOperator componentOperator;
    private ServerOperator serverOperator;
    private ServiceOperator serviceOperator;

    public OptionListController() throws IOException {
    }

    public void threadIsDead() {
        logger.info("Thread is Dead");
        threadIsAlive = false;
    }

    public void windowFocused(Boolean isWindowFocused) {
        if (isWindowFocused) {
            if (!isMinimized) {
                rootVBox.setStyle("-fx-border-color: #5accff");
                titleLabel.setStyle("-fx-text-fill: black");
            }
        } else {
            if (!isMinimized) {
                rootVBox.setStyle("-fx-border-color: #c0c0c0");
                titleLabel.setStyle("-fx-text-fill: gray");
            }
        }
    }

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
        if (update != null) { // Last version when hovered on update button
            update.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered) {
                    if (!lastComponentVersion.equals("0.0.0.0")) {
                        update.setText(lastComponentVersion);
                    }
                } else {
                    update.setText("Update");
                }
            });
        }
    }

    @FXML
    public void minWindow(MouseEvent mouseEvent) {
        dialogStage.setIconified(true);
    }

    @FXML
    public void miniWindows(MouseEvent mouseEvent) throws IOException {
        if (isMinimized) {
            dialogStage.setHeight(dialogStageHeight);
            getMinimizedButtons(false);
            isMinimized = false;
        } else {
            dialogStage.setHeight(110);
            getMinimizedButtons(true);
            isMinimized = true;
        }
    }

    @FXML
    public void setOnEntered(MouseEvent event) {
        if (isMinimized) {
            dialogStage.setHeight(110);
            titleHBox.getStylesheets().clear();
            titleHBox.getStylesheets().add(mainCSS);
        }
        isEntered = true;
    }

    @FXML
    public void setOnExited(MouseEvent event) {
        if (isMinimized) {
            if (isStoped) {
                dialogStage.setHeight(38);
                titleHBox.getStylesheets().clear();
                titleHBox.getStylesheets().add(buttonStopCSS);
            } else {
                dialogStage.setHeight(38);
                titleHBox.getStylesheets().clear();
                titleHBox.getStylesheets().add(update.getStylesheets().get(0));
            }
        }
        isEntered = false;
    }

    @FXML
    public void closeWindow(MouseEvent mouseEvent) {
        dialogStage.hide();
    }

    public void setOnPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    public void setOnDragged(MouseEvent event) {
        dialogStage.setX(event.getScreenX() - xOffset);
        dialogStage.setY(event.getScreenY() - yOffset);
    }

    public void setGuiGenerator(GUIGenerator guiGenerator, Stage dialogStage) {
        this.guiGenerator = guiGenerator;
        this.dialogStage = dialogStage;
        componentOperator = guiGenerator.getComponentOperator();
        serverOperator = guiGenerator.getServerOperator();
        serviceOperator = guiGenerator.getServiceOperator();
        componentOperator.setServiceOperator(serviceOperator);
        componentOperator.setOptionListController(this);
        serverOperator.setServiceOperator(serviceOperator);
        serverOperator.setOptionListController(this);
    }

    public void setRootData(ArrayList<Tab> projectsList) {
        projectsOverview.getTabs().addAll(projectsList);
        titleLabel.setText("kitUP" + " / " + "1.8.5");
    }

    public void setServerData(Project project, Server server) {
        pathServer = project.getServerName();
        pathSales = project.getSalesDirName();
        serviceNameQortes = server.getServiceNameQortes();
        serviceNameQortesDB = server.getServiceNameQortesDB();
        exeNameQortes = server.getExeNameQortes();
        exeNameQortesDB = server.getExeNameQortesDB();
        pathLastVersion = server.getLastVersionDirName();
        pathServerDir = server.getServerDirName();
        serverName = server.getServerName();
        titleLabel.setText(project.getProjectName() + " / " + serverName);

        Service threadMCV = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        monitorServerVersion();
                        return null;
                    }
                };
            }
        };
        threadMCV.start();

        Service threadMCS = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        monitorServerState();
                        return null;
                    }
                };
            }
        };
        threadMCS.start();
    }

    public void setComponentData(Project project, Component component) {
        pathServer = project.getServerName();
        pathSales = project.getSalesDirName();
        serviceName = component.getServiceName();
        pathLastVersion = component.getLastVersionDirName();
        pathComponent = component.getComponentDirName();
        componentName = component.getComponentName();
        titleLabel.setText(project.getProjectName() + " / " + componentName);

        Service threadMCV = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        monitorComponentVersion();
                        return null;
                    }
                };
            }
        };
        threadMCV.start();

        Service threadMCS = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        monitorComponentState();
                        return null;
                    }
                };
            }
        };
        threadMCS.start();
    }

    private void setRollbackDates(ArrayList<Button> rollbackDateButtonList) {
        dialogStageHeight = dialogStageDefaultHeight + 39 * rollbackDateButtonList.size();
        dialogStage.setHeight(dialogStageHeight);
        rollbackDates.getChildren().addAll(rollbackDateButtonList);
    }

    /**
     * MONITORING COMPONENT - START/STOP
     */
    private void monitorComponentState() {
        while (threadIsAlive) {
            componentStateNotificator("auto");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.warn("WARNING: " + e.toString());
            }
        }
    }

    /**
     * MONITORING SERVER - START/STOP
     */
    private void monitorServerState() {
        while (threadIsAlive) {
            serverStateNotificator("auto");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.warn("WARNING: " + e.toString());
            }
        }
    }

    /**
     * MONITORING COMPONENT - VERSION
     */
    private void monitorComponentVersion() {

        while (threadIsAlive) {
            String currentComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
            lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            componentVersion.setText(currentComponentVersion);
            //noinspection Duplicates
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                update.getStylesheets().clear();
                updateMin.getStylesheets().clear();
                update.getStylesheets().add(buttonGoodCSS);
                updateMin.getStylesheets().add(buttonGoodCSS);
            } else {
                update.getStylesheets().clear();
                updateMin.getStylesheets().clear();
                update.getStylesheets().add(buttonWarningCSS);
                updateMin.getStylesheets().add(buttonWarningCSS);
            }

            if (isMinimized) {
                if (!isEntered) {
                    if (!isStoped) {
                        titleHBox.getStylesheets().clear();
                        titleHBox.getStylesheets().add(update.getStylesheets().get(0));
                    } else {
                        titleHBox.getStylesheets().clear();
                        titleHBox.getStylesheets().add(buttonStopCSS);
                    }
                }
            }

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.warn("WARNING: " + e.toString());
            }
        }
    }

    /**
     * MONITORING SERVER - VERSION
     */
    private void monitorServerVersion() {

        while (threadIsAlive) {
            String currentComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
            lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            componentVersion.setText(currentComponentVersion);
            //noinspection Duplicates
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                update.getStylesheets().clear();
                updateMin.getStylesheets().clear();
                update.getStylesheets().add(buttonGoodCSS);
                updateMin.getStylesheets().add(buttonGoodCSS);
            } else {
                update.getStylesheets().clear();
                updateMin.getStylesheets().clear();
                update.getStylesheets().add(buttonWarningCSS);
                updateMin.getStylesheets().add(buttonWarningCSS);
            }

            if (isMinimized) {
                if (!isEntered) {
                    if (!isStoped) {
                        titleHBox.getStylesheets().clear();
                        titleHBox.getStylesheets().add(update.getStylesheets().get(0));
                    } else {
                        titleHBox.getStylesheets().clear();
                        titleHBox.getStylesheets().add(buttonStopCSS);
                    }
                }
            }

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.warn("WARNING: " + e.toString());
            }
        }
    }

    /**
     * COMPONENT STATE NOTIFICATOR
     */
    public void componentStateNotificator(String state) {
        Boolean serviceRunning = false;

        if (state.equals("auto")) {
            serviceRunning = serviceOperator.checkServiceState(pathServer, serviceName, "RUNNING");
        }

        if ((serviceRunning && state.equals("auto")) || state.equals("start")) { //If service running
            stop.getStylesheets().clear();
            stopMin.getStylesheets().clear();
            start.getStylesheets().clear();
            startMin.getStylesheets().clear();
            start.getStylesheets().add(buttonStartCSS);
            startMin.getStylesheets().add(buttonStartCSS);
            if (isMinimized) {
                if (!isEntered) {
                    titleHBox.getStylesheets().clear();
                    titleHBox.getStylesheets().add(buttonStartCSS);
                }
            }
            isStoped = false;
        } else {
            start.getStylesheets().clear();
            startMin.getStylesheets().clear();
            stop.getStylesheets().clear();
            stopMin.getStylesheets().clear();
            stop.getStylesheets().add(buttonStopCSS);
            stopMin.getStylesheets().add(buttonStopCSS);
            if (isMinimized) {
                if (!isEntered) {
                    titleHBox.getStylesheets().clear();
                    titleHBox.getStylesheets().add(buttonStopCSS);
                }
            }
            isStoped = true;
        }
    }

    /**
     * SERVER STATE NOTIFICATOR
     */
    public void serverStateNotificator(String state) {
        Boolean serviceRunning = false;

        if (state.equals("auto")) {
            serviceRunning = serviceOperator.checkServiceState(pathServer, serviceNameQortes, "RUNNING");
            if (serviceRunning) {
                serviceRunning = serviceOperator.checkServiceState(pathServer, serviceNameQortesDB, "RUNNING");
            }
        }

        if ((serviceRunning && state.equals("auto")) || state.equals("start")) { //Если служба запущена
            stop.getStylesheets().clear();
            start.getStylesheets().clear();
            start.getStylesheets().add("/ButtonStart.css");
        } else {
            start.getStylesheets().clear();
            stop.getStylesheets().clear();
            stop.getStylesheets().add("/ButtonStop.css");
        }
    }

    /**
     * UPDATE COMPONENT
     */
    @FXML
    private void updateComponent() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        componentOperator.updateComponent(pathServer, serviceName, pathLastVersion, pathComponent);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            String currentComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
            lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                update.getStylesheets().clear();
                updateMin.getStylesheets().clear();
                update.getStylesheets().add(buttonGoodCSS);
                updateMin.getStylesheets().add(buttonGoodCSS);

                if (isMinimized) {
                    if (!isEntered) {
                        titleHBox.getStylesheets().clear();
                        titleHBox.getStylesheets().add(buttonGoodCSS);
                    }
                }
            }
            update.setDisable(false);
            updateMin.setDisable(false);
            componentVersion.setText(currentComponentVersion);
        });

        update.setDisable(true);
        updateMin.setDisable(true);
        backqroundThread.start();
    }

    /**
     * UPDATE SERVER
     */
    @FXML
    private void updateServer() {
        Service updateServerThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        serverOperator.updateServer(pathServer, serviceNameQortes, serviceNameQortesDB, pathLastVersion, pathServerDir, exeNameQortes, exeNameQortesDB);
                        return null;
                    }
                };
            }
        };

        updateServerThread.setOnSucceeded(event -> update.setDisable(false));

        update.setDisable(true);
        updateServerThread.start();
    }

    /**
     * RESTART
     */
    @FXML
    private void restartComponent() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        componentOperator.restartComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };
        backqroundThread.setOnSucceeded(event -> {
            restart.setDisable(false);
            restartMin.setDisable(false);
        });

        restart.setDisable(true);
        restartMin.setDisable(true);
        backqroundThread.start();
    }

    /**
     * RESTART
     */
    @FXML
    private void restartServer() {
        Service restartServerThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        serverOperator.restartServer(pathServer, serviceNameQortes, serviceNameQortesDB);
                        return null;
                    }
                };
            }
        };
        restartServerThread.setOnSucceeded(event -> restart.setDisable(false));

        restart.setDisable(true);
        restartServerThread.start();
    }

    /**
     * START
     */
    @FXML
    private void startComponent() {
        Service threadStartCom = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        componentOperator.startComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };

        threadStartCom.setOnSucceeded(event -> {
            start.setDisable(false);
            startMin.setDisable(false);
        });

        start.setDisable(true);
        startMin.setDisable(true);
        threadStartCom.start();
    }

    /**
     * START
     */
    @FXML
    private void startServer() {
        Service startServerThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        serverOperator.startServer(pathServer, serviceNameQortes, serviceNameQortesDB);
                        return null;
                    }
                };
            }
        };

        startServerThread.setOnSucceeded(event -> start.setDisable(false));

        start.setDisable(true);
        startServerThread.start();
    }

    /**
     * STOP
     */
    @FXML
    private void stopComponent() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        componentOperator.stopComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            stop.setDisable(false);
            stopMin.setDisable(false);
        });

        stop.setDisable(true);
        stopMin.setDisable(true);
        backqroundThread.start();
    }

    /**
     * STOP
     */
    @FXML
    private void stopServer() {
        Service stopServerThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        serverOperator.stopServer(pathServer, serviceNameQortes, serviceNameQortesDB);
                        return null;
                    }
                };
            }
        };

        stopServerThread.setOnSucceeded(event -> stop.setDisable(false));

        stop.setDisable(true);
        stopServerThread.start();
    }

    /**
     * INI
     */
    @FXML
    private void openIni() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        String link = pathComponent + componentName + ".ini";
                        guiGenerator.getFileOpener().openFile(link);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> ini.setDisable(false));

        ini.setDisable(true);
        backqroundThread.restart();
    }

    /**
     * LOG
     */
    @FXML
    private void openLog() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        String actualLogName = componentOperator.getComponentLogName(componentName, pathComponent);
                        String link = pathComponent + actualLogName;
                        guiGenerator.getFileOpener().openFile(link);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> log.setDisable(false));

        log.setDisable(true);
        backqroundThread.restart();
    }

    /**
     * DIR
     */
    @FXML
    private void openDir() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        String link = pathComponent;
                        guiGenerator.getFileOpener().openDir(link);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> dir.setDisable(false));

        dir.setDisable(true);
        backqroundThread.restart();
    }

    /**
     * Rollback ADD/UPDATE
     */
    @FXML
    private void getRollbackDates() {
        /*
         * Запуск создания кнопок для отката компоненты в отдельном потоке
         */
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        ArrayList<Button> rollbackDateButtonList = new ArrayList<>();
                        final Boolean[] isNeedFixVersion = {false};
                        final String[] pathFixVersion = new String[1];

                        for (Map.Entry<String, String> entry : componentOperator.returnRollbackDates(pathLastVersion).entrySet()) {
                            String pastFolder = componentOperator.getPathPastVersion(pathSales, entry.getKey());
                            if (pastFolder == null) { //Если папки с откатом не найдено, то не добавляем кнопку
                                continue;
                            }
                            String pathPastVersion = pathSales + pastFolder + "\\" + "SrvComp" + "\\" + componentName + "\\";
                            System.out.println("Путь к откату: " + pathPastVersion);
                            Button button = new Button();
                            Button fixButton = new Button();

                            fixButton.getStylesheets().add("/FixButton.css");
                            fixButton.setText("fix");
                            fixButton.setOnAction(e -> {
                                button.setDisable(true);
                                isNeedFixVersion[0] = true;
                                pathFixVersion[0] = pathLastVersion + "\\" + entry.getValue();
                            });

                            button.setGraphic(fixButton);
                            button.setText(entry.getKey());
                            button.setPrefWidth(108);
                            button.setOnAction(
                                    (e) -> {
                                        button.getStylesheets().add("/MainStylesheet.css");
                                        button.setDisable(true);
                                        /*
                                         * Запуск отката компоненты в отдельном потоке
                                         */
                                        Service rollbackThread = new Service<Void>() {
                                            protected Task<Void> createTask() {
                                                return new Task<>() {
                                                    protected Void call() {
                                                        if (isNeedFixVersion[0]) {
                                                            componentOperator.rollbackComponent(pathServer, serviceName, pathComponent, pathPastVersion, pathFixVersion[0]);
                                                        } else {
                                                            componentOperator.rollbackComponent(pathServer, serviceName, pathComponent, pathPastVersion, null);
                                                        }
                                                        return null;
                                                    }
                                                };
                                            }
                                        };

                                        rollbackThread.setOnSucceeded(event -> {
                                            button.setDisable(false);
                                            isNeedFixVersion[0] = false;
                                            String currentComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
                                            lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
                                            componentVersion.setText(currentComponentVersion);
                                            //noinspection Duplicates
                                            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                                                update.getStylesheets().clear();
                                                updateMin.getStylesheets().clear();
                                                update.getStylesheets().add(buttonGoodCSS);
                                                updateMin.getStylesheets().add(buttonGoodCSS);

                                            } else {
                                                update.getStylesheets().clear();
                                                updateMin.getStylesheets().clear();
                                                update.getStylesheets().add(buttonWarningCSS);
                                                updateMin.getStylesheets().add(buttonWarningCSS);
                                            }
                                        });

                                        rollbackThread.start();
                                    }
                            );

                            rollbackDateButtonList.add(button);
                        }
                        /*
                         * Передаем обновление GUI (добавление новых кнопок) в основной поток
                         */
                        Platform.runLater(() -> setRollbackDates(rollbackDateButtonList));
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> { //После завершения вернуть кнопку в обычное состояние
            getRollbackDates.setDisable(false);
        });

        rollbackDates.getChildren().clear();
        getRollbackDates.setDisable(true);
        backqroundThread.start();
    }

    private void getMinimizedButtons(Boolean isMinimized) {
        if (contentHBox != null) {
            if (isMinimized) {
                updateMin.setText("U");
                updateMin.setPrefWidth(100);
                updateMin.setOnAction((e) -> updateComponent());

                restartMin.setText("R");
                restartMin.setPrefWidth(100);
                restartMin.setOnAction((e) -> restartComponent());

                startMin.setText("S");
                startMin.setPrefWidth(100);
                startMin.setOnAction((e) -> startComponent());

                stopMin.setPrefWidth(100);
                stopMin.setPrefHeight(30.0);
                stopMin.setAlignment(Pos.CENTER);
                Rectangle rectStopMin = new Rectangle();
                rectStopMin.setHeight(10.0);
                rectStopMin.setWidth(10.0);
                stopMin.setGraphic(rectStopMin);
                stopMin.setOnAction((e) -> stopComponent());

                contentHBox.setPadding(new Insets(0, 10, 10, 10));
                contentHBox.getChildren().addAll(updateMin, restartMin, startMin, stopMin);
            } else {
                contentHBox.setPadding(new Insets(0, 0, 0, 0));
                contentHBox.getChildren().clear();
            }
        }
    }
}
