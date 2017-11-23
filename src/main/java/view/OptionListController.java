package view;

import bin.GUIGenerator;
import classroom.Component;
import classroom.Project;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ComponentOperator;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class OptionListController {
    private static final Logger logger = LogManager.getLogger(OptionListController.class);
    @FXML
    VBox rootVBox;

    @FXML
    HBox titleHBox;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Label titleLabel;

    @FXML
    private Button update;

    @FXML
    private Button restart;

    @FXML
    private Button start;

    @FXML
    private Button stop;

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

    private String pathServer;
    private String pathSales;
    private String serviceName;
    private String pathLastVersion;
    private String pathComponent;
    private String componentName;

    private Boolean threadIsAlive = true;

    private Stage dialogStage;
    private GUIGenerator guiGenerator;
    private ComponentOperator componentOperator;

    public OptionListController() {
    }

    public void threadIsDead() {
        logger.info("Thread is Dead");
        threadIsAlive = false;
    }

    public void windowFocused(Boolean isWindowFocused) {
        if (isWindowFocused) {
            rootVBox.setStyle("-fx-border-color: #5accff");
            titleLabel.setStyle("-fx-text-fill: black");
        } else {
            rootVBox.setStyle("-fx-border-color: #c0c0c0");
            titleLabel.setStyle("-fx-text-fill: gray");
        }
    }

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
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
        componentOperator.setOptionListController(this);
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
        dialogStage.setHeight(328 + 39 * rollbackDateButtonList.size());
        rollbackDates.getChildren().addAll(rollbackDateButtonList);
    }

    /**
     * MONITORING COMPONENT - START/STOP
     */
    private void monitorComponentState() {
        while (threadIsAlive) {
            componentStateNotificator("auto");

            try {
                Thread.sleep(15000);
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
            String lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            componentVersion.setText(currentComponentVersion);
            //noinspection Duplicates
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                update.getStylesheets().clear();
                update.getStylesheets().add("/ButtonGood.css");
            } else {
                update.getStylesheets().clear();
                update.getStylesheets().add("/ButtonWarning.css");
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
            serviceRunning = componentOperator.checkServiceState(pathServer, serviceName, "RUNNING");
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
     * UPDATE
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
            update.setDisable(false);
            String currentComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
            String lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                update.getStylesheets().clear();
                update.getStylesheets().add("/ButtonGood.css");
            }
            componentVersion.setText(currentComponentVersion);
        });

        update.setDisable(true);
        backqroundThread.restart();
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
        backqroundThread.setOnSucceeded(event -> restart.setDisable(false));

        restart.setDisable(true);
        backqroundThread.restart();
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

        threadStartCom.setOnSucceeded(event -> start.setDisable(false));

        start.setDisable(true);
        threadStartCom.restart();
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

        backqroundThread.setOnSucceeded(event -> stop.setDisable(false));

        stop.setDisable(true);
        backqroundThread.restart();
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

                            fixButton.setStyle("-fx-padding: 0 7 0 7");
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
                                            String lastComponentVersion = componentOperator.getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
                                            componentVersion.setText(currentComponentVersion);
                                            //noinspection Duplicates
                                            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                                                update.getStylesheets().clear();
                                                update.getStylesheets().add("/ButtonGood.css");

                                            } else {
                                                update.getStylesheets().clear();
                                                update.getStylesheets().add("/ButtonWarning.css");
                                            }
                                        });

                                        rollbackThread.restart();
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
        backqroundThread.restart();
    }


}
