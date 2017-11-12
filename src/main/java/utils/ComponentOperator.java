package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private FileInformator fileInformator;

    public ComponentOperator() {
        fileInformator = new FileInformator();
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
        FileCopyer copyFiles = new FileCopyer();

        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        copyFiles.copyFiles(pathLastVersion, pathComponent);
        startComponent(pathServer, serviceName);
    }

    /**
     * RESTART Component
     */
    public void restartComponent(String pathServer, String serviceName) {
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName);
        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        startComponent(pathServer, serviceName);
    }

    /**
     * START Component
     */
    public void startComponent(String pathServer, String serviceName) { //Запуск компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName);
        cmdRun(pathServer, serviceName, " start ");
    }

    /**
     * STOP Component
     */
    public void stopComponent(String pathServer, String serviceName) { //Остановка компоненты
        log.info("pathServer: " + pathServer + " | "
                + "serviceName " + serviceName);
        cmdRun(pathServer, serviceName, " stop ");
    }

    /**
     * ROLLBACK Component
     */
    public void rollbackComponent(String pathServer, String serviceName, String pathComponent, String pathPastVersion) {
        log.info("pathServer: " + pathServer + " | "
                + "serviceName: " + serviceName + " | "
                + "pathComponent: " + pathComponent + " | "
                + "pathPastVersion: " + pathPastVersion);
        FileCopyer copyFiles = new FileCopyer();

        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        copyFiles.copyFiles(pathPastVersion, pathComponent);
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
            log.warn("Error: " + e.toString());
        }
    }

    /**
     * CHECK SERVICE - STOP
     * Запрос состояния службы (ожидание остановки)
     */
    private void checkServiceStop(String pathServer, String serviceName) {
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + " query " + serviceName + " | find \"STOPPED\"");
        try {
            for (int i = 0; ; i++) { // Запрашиваем состояние службы
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "sc " + pathServer + " query " + serviceName + " | find \"STOPPED\"");
                Process p = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"));
                String line = reader.readLine();
                if (line == null) { //Если служба не остановлена
                    log.info("Wait stoping service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба остановлена
                    log.info(line);
                    log.info("....Service " + serviceName + " is stoped");
                    break;
                }

                if (i == 5) { // Если компонента не остановлена в течении 10 секунд, повторяем попытку
                    stopComponent(pathServer, serviceName);
                    i = 0;
                }
            }
        } catch (IOException | InterruptedException e) {
            log.warn("Error: " + e.toString());
        }
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

        Pattern p = Pattern.compile("[0-9]+");
        log.info("Get rollback dates....");
        log.info(pathLastVersion);

        if (listOfFiles != null) {
            int i = 0;
            for (File file : listOfFiles) {
                if (0 < file.getName().lastIndexOf("_")) {
                    String s_ = file.getName().substring(0, (file.getName().lastIndexOf("_")));
                    Matcher m_ = p.matcher(s_);
                    if (m_.matches()) {
                        log.info(s_);
                        i++;
                        rollbackDateButtonList.add(s_);
                    }
                } else {
                    String s = file.getName();
                    Matcher m = p.matcher(s);
                    if (m.matches()) {
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
        File[] listOfFiles = folder.listFiles(File::isDirectory);
        log.info("Get rollback path....");
        log.info(pathSales);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                log.info(file.getName());
                if (file.getName().contains("Full_" + rollbackDate)) {
                    log.info("Подходящая папка найдена: " + file.getName());
                    return file.getName();
                }
            }
        }
        return null;
    }
}
