package utils;

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

    public ComponentOperator() {
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
        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        startComponent(pathServer, serviceName);
    }

    /**
     * START Component
     */
    public void startComponent(String pathServer, String serviceName) { //Запуск компоненты
        System.out.println(pathServer + " " + serviceName);
        cmdRun(pathServer, serviceName, " start ");
    }

    /**
     * STOP Component
     */
    public void stopComponent(String pathServer, String serviceName) { //Остановка компоненты
        System.out.println(pathServer + " " + serviceName);
        cmdRun(pathServer, serviceName, " stop ");
    }

    /**
     * ROLLBACK Component
     */
    public void rollbackComponent(String pathServer, String serviceName, String pathLastVersion, String pathComponent, String pathPastVersion) {
        FileCopyer copyFiles = new FileCopyer();

        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        copyFiles.copyFiles(pathPastVersion, pathComponent);
        copyFiles.copyFiles(pathLastVersion, pathComponent);
        startComponent(pathServer, serviceName);
    }

    /**
     * Отправить команду в cmd
     *
     * @param action - Действие (start, stop)
     */
    private void cmdRun(String pathServer, String serviceName, String action) { //Запуск команды в консоль
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "sc " + pathServer + action + serviceName);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866")); //Кодировка CP866 т.к. ее для вывода исторически использует консоль
            String line;
            for (line = reader.readLine(); line != null; line = reader.readLine()) { //Выводим ответ с консоли
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Запрос состояния службы (ожидание остановки)
     */
    private void checkServiceStop(String pathServer, String serviceName) {
        try {
            for (; ; ) { //Запрашиваем состояние службы
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "sc " + pathServer + " query " + serviceName + " | find \"STOPPED\"");
                Process p = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"));
                String line = reader.readLine();
                if (line == null) { //Если служба не остановлена
                    System.out.println("Wait stoping service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба остановлена
                    System.out.println(line);
                    System.out.println("....Service " + serviceName + " is stoped");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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

        // Sort files by name desc
        if (listOfFiles != null) {
            Arrays.sort(listOfFiles, Comparator.comparing(File::getName, reverseOrder()));
        }

        Pattern p = Pattern.compile("[0-9]");
        System.out.println("Get rollback dates....");
        System.out.println(pathLastVersion);

        if (listOfFiles != null) {
            int i = 0;
            for (File file : listOfFiles) {
                if (0 < file.getName().lastIndexOf("_")) {
                    Matcher m = p.matcher(file.getName().substring(0, (file.getName().lastIndexOf("_"))));

                    if (!m.matches()) { //
                        System.out.println(file.getName().substring(0, (file.getName().lastIndexOf("_"))));
                        i++;
                        rollbackDateButtonList.add(file.getName().substring(0, (file.getName().lastIndexOf("_"))));
                        if (i == 3) {
                            break;
                        }
                    }
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
    public String GetPathPastVersion(String pathSales, String rollbackDate) {
        File folder = new File(pathSales);
        File[] listOfFiles = folder.listFiles(File::isDirectory);
        System.out.println("Get rollback path....");
        System.out.println(pathSales);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                System.out.println(file.getName());
                if (file.getName().contains("Full_" + rollbackDate)) {
                    System.out.println("Подходящая папка найдена: " + file.getName());
                    return file.getName();
                }
            }
        }
        return null;
    }
}
