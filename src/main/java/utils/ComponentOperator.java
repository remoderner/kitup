package utils;

import classroom.Component;
import classroom.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ComponentOperator {

    public ComponentOperator() {
    }

    void updateComponent(Project project, Component component) { //Рестарт + обновление компоненты
        String pathServer = project.getServerName();
        String serviceName = component.getServiceName();
        String pathLast = component.getSourceDirName();
        String pathTarget = component.getTargetSourceDir();

        FileCopyer copyFiles = new FileCopyer();

        stopComponent(pathServer, serviceName);
        checkServiceStop(pathServer, serviceName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        copyFiles.copyFiles(pathLast, pathTarget);
        startComponent(pathServer, serviceName);
    }

    private void startComponent(String pathServer, String serviceName) { //Запуск компоненты
        System.out.println(pathServer + " " + serviceName);
        cmdRun(pathServer, serviceName, " start ");
    }

    private void stopComponent(String pathServer, String serviceName) { //Остановка компоненты
        System.out.println(pathServer + " " + serviceName);
        cmdRun(pathServer, serviceName, " stop ");
    }

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

    private void checkServiceStop(String pathServer, String serviceName) { //Проверка остановки службы
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
}
