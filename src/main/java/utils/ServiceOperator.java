package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServiceOperator {
    private static final Logger log = LogManager.getLogger(ComponentOperator.class);

    /**
     * Отправить команду в cmd
     *
     * @param action - Действие (start, stop)
     */
    void cmdRun(String pathServer, String serviceName, String action) { //Запуск команды в консоль
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
    Boolean waitServiceStop(String pathServer, String serviceName) {
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + " query " + serviceName + " | find \"STOPPED\"");
        try {
            for (int i = 0; ; i++) { // Запрашиваем состояние службы
                Boolean serviceStopped = checkServiceState(pathServer, serviceName, "STOPPED");
                if (!serviceStopped) { //Если служба не остановлена
                    log.info("Wait stoping service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба остановлена
                    log.info("....Service " + serviceName + " is stoped");
                    return true;
                }

                if (i == 5) { // Если компонента не остановлена в течении 10 секунд
                    return false;
                }
            }
        } catch (InterruptedException e) {
            log.warn("WARNING: " + e.toString());
        }
        return false;
    }

    /**
     * WAIT SERVICE START
     * Запрос состояния службы (ожидание запуска)
     */
    Boolean waitServiceStart(String pathServer, String serviceName) {
        log.info("cmd.exe: " + "/c " + "sc " + pathServer + " query " + serviceName + " | find \"RUNNING\"");
        try {
            for (int i = 0; ; i++) { // Запрашиваем состояние службы
                Boolean serviceStarted = checkServiceState(pathServer, serviceName, "RUNNING");
                if (!serviceStarted) { //Если служба не запущена
                    log.info("Wait running service " + serviceName + "....");
                    Thread.sleep(2000);
                } else { //Если служба запущена
                    log.info("....Service " + serviceName + " is running");
                    return true;
                }

                if (i == 5) { // Если компонента не запущена в течении 10 секунд
                    return false;
                }
            }
        } catch (InterruptedException e) {
            log.warn("WARNING: " + e.toString());
        }
        return false;
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
}
