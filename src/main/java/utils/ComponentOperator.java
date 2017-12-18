package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.OptionListController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.reverseOrder;

public class ComponentOperator {
    private static final Logger log = LogManager.getLogger(ComponentOperator.class);
    private FileInformator fileInformator = new FileInformator();
    private FileOperator fileOperator = new FileOperator();
    private ServiceOperator serviceOperator;
    private OptionListController optionListController;

    public void setServiceOperator(ServiceOperator serviceOperator) {
        this.serviceOperator = serviceOperator;
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
        fileOperator.copyFiles(pathLastVersion, pathComponent, null, false);
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

        for (int i = 0; i < 5; i++) { // 5 попыток (50 секунд) на старт компоненты
            serviceOperator.cmdRun(pathServer, serviceName, " start ");
            if (serviceOperator.waitServiceStart(pathServer, serviceName)) {
                optionListController.componentStateNotificator("start");
                break;
            }
        }
    }

    /**
     * STOP Component
     */
    public void stopComponent(String pathServer, String serviceName) { //Остановка компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName " + serviceName);

        for (int i = 0; i < 5; i++) { // 5 попыток (50 секунд) на остановку компоненты
            serviceOperator.cmdRun(pathServer, serviceName, " stop ");
            if (serviceOperator.waitServiceStop(pathServer, serviceName)) {
                optionListController.componentStateNotificator("stop");
                break;
            }
        }
    }

    /**
     * ROLLBACK Component
     */
    public void rollbackComponent(String pathServer, String serviceName, String pathComponent, String pathSource, String pathFixVersion) {
        ArrayList<String> alreadyCopiedFiles = new ArrayList<>();
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName + " | "
                + "pathComponent: " + pathComponent + " | "
                + "pathSource: " + pathSource);

        stopComponent(pathServer, serviceName);
        fileOperator.deleteFile(pathComponent + "Starter.log");
        if (pathFixVersion != null) {
            fileOperator.copyFiles(pathFixVersion, pathComponent, null, true);
            alreadyCopiedFiles = fileOperator.findFiles(pathFixVersion);
        }
        fileOperator.copyFiles(pathSource, pathComponent, alreadyCopiedFiles, true);
        startComponent(pathServer, serviceName);
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

    public LinkedHashMap<String, String> returnRollbackDates(String pathLastVersion) {
        LinkedHashMap<String, String> rollbackDateButtonList = new LinkedHashMap<>();
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
                        log.info(s + " | " + file.getName());
                        rollbackDateButtonList.put(s, file.getName());
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
