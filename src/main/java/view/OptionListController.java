package view;

import bin.GUIGenerator;
import classroom.Component;
import classroom.Project;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.FileOpener;

import java.util.ArrayList;
import java.util.Objects;

public class OptionListController {

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

    private Stage dialogStage;
    private GUIGenerator guiGenerator; // Ссылка на главное приложение.
    private FileOpener fileOpener;

    public OptionListController() {
    }

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {
    }

    public void setGuiGenerator(GUIGenerator guiGenerator, Stage dialogStage) {
        this.guiGenerator = guiGenerator;
        this.dialogStage = dialogStage;
    }

    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }

    public void setComponentData(Project project, Component component) {
        pathServer = project.getServerName();
        pathSales = project.getSalesDirName();
        serviceName = component.getServiceName();
        pathLastVersion = component.getLastVersionDirName();
        pathComponent = component.getComponentDirName();
        componentName = component.getComponentName();
        String currentComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
        String lastComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
        componentVersion.setText(currentComponentVersion);

        if (!Objects.equals(currentComponentVersion, lastComponentVersion)) {
            update.getStylesheets().add("/ButtonWarning.css");
        } else {
            update.getStylesheets().add("/ButtonGood.css");
        }
    }

    private void setRollbackDates(ArrayList<Button> rollbackDateButtonList) {
        dialogStage.setHeight(330 + 37 * rollbackDateButtonList.size());
        rollbackDates.getChildren().addAll(rollbackDateButtonList);
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
                        guiGenerator.getComponentOperator().updateComponent(pathServer, serviceName, pathLastVersion, pathComponent);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            update.setStyle(null);
            update.setDisable(false);
            String currentComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
            String lastComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
            if (Objects.equals(currentComponentVersion, lastComponentVersion)) {
                Platform.runLater(() -> update.getStylesheets().add("/ButtonGood.css"));
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
                        guiGenerator.getComponentOperator().restartComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            restart.setStyle(null);
            restart.setDisable(false);
        });

        restart.setDisable(true);
        backqroundThread.restart();
    }

    /**
     * START
     */
    @FXML
    private void startComponent() {
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        guiGenerator.getComponentOperator().startComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            start.setStyle(null);
            start.setDisable(false);
        });

        start.setDisable(true);
        backqroundThread.restart();
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
                        guiGenerator.getComponentOperator().stopComponent(pathServer, serviceName);
                        return null;
                    }
                };
            }
        };

        backqroundThread.setOnSucceeded(event -> {
            stop.setStyle(null);
            stop.setDisable(false);
        });

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
                        fileOpener.openFile(link);
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
                        String actualLogName = guiGenerator.getComponentOperator().getComponentLogName(componentName, pathComponent);
                        String link = pathComponent + actualLogName;
                        fileOpener.openFile(link);
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
                        fileOpener.openDir(link);
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
                        for (String entry : guiGenerator.getComponentOperator().getRollbackDates(pathLastVersion)) {
                            String pastFolder = guiGenerator.getComponentOperator().getPathPastVersion(pathSales, entry);
                            if (pastFolder == null) { //Если папки с откатом не найдено, то не добавляем кнопку
                                continue;
                            }
                            String pathPastVersion = pathSales + pastFolder + "\\" + "SrvComp" + "\\" + componentName;
                            System.out.println("Путь к откату: " + pathPastVersion);
                            Button button = new Button();
                            button.setText(entry);
                            button.setPrefWidth(108);
                            button.setOnAction(
                                    (e) -> {
                                        button.getStylesheets().add("/MainStylesheet.css");
                                        button.setDisable(true);
                                        /*
                                         * Запуск отката компоненты в отдельном потоке
                                         */
                                        Service backqroundThread = new Service<Void>() {
                                            protected Task<Void> createTask() {
                                                return new Task<>() {
                                                    protected Void call() {
                                                        guiGenerator.getComponentOperator().rollbackComponent(pathServer, serviceName, pathComponent, pathPastVersion);
                                                        return null;
                                                    }
                                                };
                                            }
                                        };

                                        backqroundThread.setOnSucceeded(event -> {
                                            button.setDisable(false);
                                            String currentComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathComponent, "FileVersion");
                                            String lastComponentVersion = guiGenerator.getComponentOperator().getComponentVersion(componentName + ".dll", pathLastVersion, "FileVersion");
                                            if (!Objects.equals(currentComponentVersion, lastComponentVersion)) {
                                                update.getStylesheets().add("/ButtonWarning.css");
                                            } else {
                                                update.getStylesheets().add("/ButtonGood.css");
                                            }
                                        });

                                        backqroundThread.restart();
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
