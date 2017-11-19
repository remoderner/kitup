package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.OptionListController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.reverseOrder;

public class ComponentOperator {
    private static final Logger log = LogManager.getLogger(ComponentOperator.class);
    private FileInformator fileInformator = new FileInformator();
    private FileOperator fileOperator = new FileOperator();
    private OptionListController optionListController;

    public ComponentOperator() {
    }

    public void setOptionListController(OptionListController optionListController) {
        this.optionListController = optionListController;
    }

    /**
     * UPDATE Component
     *
     * @param pathServer      - Пусть к серверу
     * @param serviceName     - Имя сервиса компоненты
     * @param pathLastVersion - Путь к папке с последними версиями
     * @param pathComponent   - Путь к папке с компонентой
     */
    public void updateComponent(String pathServer, String serviceName, String pathLastVersion, String pathComponent) { //Рестарт + обновление компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName + " | "
                + "pathLastVersion " + pathLastVersion + " | "
                + "pathComponent: " + pathComponent);

        stopComponent(pathServer, serviceName);
        fileOperator.deleteFile(pathComponent + "Starter.log");
        fileOperator.copyFiles(pathLastVersion, pathComponent);
        startComponent(pathServer, serviceName);
    }

    /**
     * RESTART Component
     */
    public void restartComponent(String pathServer, String serviceName) {
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName);
        stopComponent(pathServer, serviceName);
        startComponent(pathServer, serviceName);
    }

    /**
     * START Component
     */
    public void startComponent(String pathServer, String serviceName) { //Запуск компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName);
        cmdRun(pathServer, serviceName, " start ");
        waitServiceStart(pathServer, serviceName);
    }

    /**
     * STOP Component
     */
    public void stopComponent(String pathServer, String serviceName) { //Остановка компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName " + serviceName);
        cmdRun(pathServer, serviceName, " stop ");
        waitServiceStop(pathServer, serviceName);
    }

    /**
     * ROLLBACK Component
     */
    public void rollbackComponent(String pathServer, String serviceName, String pathComponent, String pathPastVersion) {
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName + " | "
                + "pathComponent: " + pathComponent + " | "
                + "pathPastVersion: " + pathPastVersion);

        stopComponent(pathServer, serviceName);
        fileOperator.deleteFile(pathComponent + "Starter.log");
        fileOperator.copyFiles(pathPastVersion, pathComponent);
        startComponent(pathServer, serviceName);
    }

    /**
     * Отправить команду в cmd
     *
     * @param action - Действие (start, stop)
     */
    private void cmdRun(String pathServer, String serviceName, String action) { //Запуск команды в консоль
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + action + serviceName);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "sc " + pathServer + action + serviceName);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866")); //Кодировка CP866 т.к. ее для вывода исторически использует консоль
            String line;
            for (line = reader.readLine(); line != null; line = reader.readLine()) { //Выводим ответ с консоли
                log.info(line);
            }
        } catch (IOException e) {
            log.warn("WARNING: " + e.toString());
        }
    }

    /**
     * WAIT SERVICE STOP
     * Запрос состояния службы (ожидание остановки)
     */
    private void waitServiceStop(String pathServer, String serviceName) {
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + " query " + serviceName + " | find \"STOPPED\"");
        int stopTryAmount = 1;
        try {
            for (int i = 0; ; i++) { // Запрашиваем состояние службы
                Boolean serviceStopped = checkServiceState(pathServer, serviceName, "STOPPED");
                if (!serviceStopped) { //Если служба не остановлена
                    log.info("Wait stoping service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба остановлена
                    log.info("....Service " + serviceName + " is stoped");
                    optionListController.componentStateNotificator("stop");
                    break;
                }

                if (i == 5) { // Если компонента не остановлена в течении 10 секунд, повторяем попытку
                    stopComponent(pathServer, serviceName);
                    i = 0;
                    stopTryAmount++;
                }

                //Если комопнента не остановлена после 5 попыток
                if (stopTryAmount == 5) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("WARNING: " + e.toString());
        }
    }

    /**
     * WAIT SERVICE START
     * Запрос состояния службы (ожидание запуска)
     */
    private void waitServiceStart(String pathServer, String serviceName) {
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + " query " + serviceName + " | find \"RUNNING\"");
        int startTryAmount = 1;
        try {
            for (int i = 0; ; i++) { // Запрашиваем состояние службы
                Boolean serviceStarted = checkServiceState(pathServer, serviceName, "RUNNING");
                if (!serviceStarted) { //Если служба не запущена
                    log.info("Wait running service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба запущена
                    log.info("....Service " + serviceName + " is running");
                    optionListController.componentStateNotificator("start");
                    break;
                }

                if (i == 5) { // Если компонента не запущена в течении 10 секунд, повторяем попытку
                    startComponent(pathServer, serviceName);
                    i = 0;
                    startTryAmount++;
                }

                //Если комопнента не запущена после 5 попыток
                if (startTryAmount == 5) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("WARNING: " + e.toString());
        }
    }

    /**
     * CHECK COMPONENT STATE
     */
    public Boolean checkServiceState(String pathServer, String serviceName, String checkState) {
        String line = null;
        Boolean checkResult = false;
        log.info(pathServer + " | " + serviceName + " | " + checkState);

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "sc " + pathServer + " query " + serviceName + " | find \"" + checkState + "\"");
            Process p = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"));
            line = reader.readLine();
        } catch (IOException e) {
            log.warn("WARNING: " + e.toString());
        }

        if (line != null) {
            checkResult = true;
        }

        log.info(checkResult);
        return checkResult;
    }

    /**
     * GET COMPONENT VERSION
     *
     * Вернуть ссылка на актульный лог компоненты
     */
    public String getComponentVersion(String componentName, String pathComponent, String infoType) {
        String fileVersion = fileInformator.getFileVersion(pathComponent + componentName, infoType);
        log.info(componentName + ": " + fileVersion);
        return fileVersion;
    }

    /**
     * GET COMPONENT LOG LINK
     */
    public String getComponentLogName(String componentName, String pathComponent) {
        log.info("pathComponent: " + pathComponent);
        File folder = new File(pathComponent);
        File[] listOfFiles = folder.listFiles(File::isFile);

        // Сортировка названий папок по убыванию
        if (listOfFiles != null) {
            Arrays.sort(listOfFiles, Comparator.comparing(File::lastModified, reverseOrder()));
        }

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                log.info(file.getName());
                if (file.getName().contains(componentName) && file.getName().contains(".log")) {
                    log.info("Подходящий файл найден: " + file.getName());
                    return file.getName();
                }
            }
        }

        return null;
    }

    /**
     * Вернуть даты для откатов (последние 3)
     *
     * @return rollbackDateButtonList
     */
    public ArrayList<String> getRollbackDates(String pathLastVersion) {
        ArrayList<String> rollbackDateButtonList = new ArrayList<>();
        File folder = new File(pathLastVersion);
        File[] listOfFiles = folder.listFiles(File::isDirectory);

        // Сортировка названий папок по убыванию
        if (listOfFiles != null) {
            Arrays.sort(listOfFiles, Comparator.comparing(File::getName, reverseOrder()));
        }

        Pattern p = Pattern.compile("-?\\d+");
        log.info("Get rollback dates....");
        log.info(pathLastVersion);

        if (listOfFiles != null) {
            int i = 0;
            for (File file : listOfFiles) {
                String s = "";
                Matcher m = p.matcher(file.getName());
                while (m.find()) {
                    s = s + m.group();
                    if (s.length() == 8) {
                        i++;
                        log.info(s);
                        rollbackDateButtonList.add(s);
                    }
                }

                if (i == 3) {
                    break;
                }
            }
        }

        return rollbackDateButtonList;
    }

    /**
     * Вернуть имя папки, где храниться нужный откат
     *
     * @param pathSales    - Путь к папке с прошлыми отправками
     * @param rollbackDate - Дата отката
     * @return file.getName();
     */
    public String getPathPastVersion(String pathSales, String rollbackDate) {
        File folder = new File(pathSales);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.contains("Full_" + rollbackDate));
        log.info("Get rollback path....");
        log.info(pathSales);

        if (listOfFiles != null) {
            if (listOfFiles.length == 1) {
                log.info("Подходящая папка найдена: " + listOfFiles[0].getName());
                return listOfFiles[0].getName();
            }
        }
        return null;
    }
}
