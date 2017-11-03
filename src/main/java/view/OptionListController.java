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
import javafx.stage.Stage;

import java.util.ArrayList;

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
    private VBox rollbackDates;

    @FXML
    private Button getRollbackDates;

    private String pathServer;
    private String pathSales;
    private String serviceName;
    private String pathLastVersion;
    private String pathComponent;
    private String componentName;

    private Stage dialogStage;
    private GUIGenerator guiGenerator; // Ссылка на главное приложение.

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

    public void setComponentData(Project project, Component component) {
        pathServer = project.getServerName();
        pathSales = project.getSalesDirName();
        serviceName = component.getServiceName();
        pathLastVersion = component.getLastVersionDirName();
        pathComponent = component.getComponentDirName();
        componentName = component.getComponentName();
    }

    private void setRollbackDates(ArrayList<Button> rollbackDateButtonList) {
        dialogStage.setHeight(274 + 35 * rollbackDateButtonList.size());
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
        });

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

        backqroundThread.restart();
    }

    /**
     * Rollback ADD/UPDATE
     */
    @FXML
    private void getRollbackDates() {
        getRollbackDates.setStyle("-fx-background-color: #00FA9A;");
        getRollbackDates.setDisable(true);
        rollbackDates.getChildren().clear(); //Удалить кнопки откатов
        Service backqroundThread = new Service<Void>() {
            protected Task<Void> createTask() {
                return new Task<>() {
                    protected Void call() {
                        ArrayList<Button> rollbackDateButtonList = new ArrayList<>();
                        for (String entry : guiGenerator.getComponentOperator().getRollbackDates(pathLastVersion)) {
                            String pastFolder = guiGenerator.getComponentOperator().GetPathPastVersion(pathSales, entry);
                            if (pastFolder == null) { //Если папки с откатом не найдено, то не добавляем кнопку
                                continue;
                            }
                            String pathPastVersion = pathSales + "\\" + pastFolder + "\\" + "SrvComp" + "\\" + componentName;
                            System.out.println("Путь к откату: " + pathPastVersion);
                            Button button = new Button();
                            button.setText(entry);
                            button.setPrefWidth(108);
                            button.setOnAction(
                                    (e) -> {
                                        button.setStyle("-fx-background-color: #00FA9A;");
                                        button.setDisable(true);
                                        Service backqroundThread = new Service<Void>() {
                                            protected Task<Void> createTask() {
                                                return new Task<>() {
                                                    protected Void call() {
                                                        guiGenerator.getComponentOperator().rollbackComponent(pathServer, serviceName, pathLastVersion, pathComponent, pathPastVersion);
                                                        return null;
                                                    }
                                                };
                                            }
                                        };

                                        backqroundThread.setOnSucceeded(event -> {
                                            button.setStyle(null);
                                            button.setDisable(false);
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

        backqroundThread.setOnSucceeded(event -> {
            getRollbackDates.setStyle(null);
            getRollbackDates.setDisable(false);
        });

        backqroundThread.restart();
    }
}
