package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.OptionListController;

public class ServerOperator {
    private static final Logger log = LogManager.getLogger(ComponentOperator.class);
    private FileOperator fileOperator = new FileOperator();
    private FileInformator fileInformator = new FileInformator();
    private ServiceOperator serviceOperator;
    private OptionListController optionListController;

    public void setServiceOperator(ServiceOperator serviceOperator) {
        this.serviceOperator = serviceOperator;
    }

    public void setOptionListController(OptionListController optionListController) {
        this.optionListController = optionListController;
    }

    /**
     * UPDATE Server
     *
     * @param pathServer          - Пусть к серверу
     * @param serviceNameQortes   - Имя сервиса Qortes
     * @param serviceNameQortesDB - Имя сервиса QortesDB
     * @param pathLastVersion     - Путь к папке с последними версиями
     * @param pathServerDir       - Путь к папке с сервером
     */
    public void updateServer(String pathServer, String serviceNameQortes, String serviceNameQortesDB, String pathLastVersion, String pathServerDir, String exeNameQortes, String exeNameQortesDB) { //Рестарт + обновление сервера
        log.info("pathServer: " + pathServer + " | "
                + "serviceNameQortes: " + serviceNameQortes + " | "
                + "serviceNameQortesDB: " + serviceNameQortesDB + " | "
                + "pathLastVersion " + pathLastVersion + " | "
                + "pathServerDir: " + pathServerDir);

        stopServer(pathServer, serviceNameQortes, serviceNameQortesDB);
        fileOperator.deleteFile(pathServerDir + exeNameQortes);
        fileOperator.deleteFile(pathServerDir + exeNameQortesDB);
        fileOperator.copyFile(pathLastVersion + "Qortes.exe", pathServerDir);
        fileOperator.copyFile(pathLastVersion + "QortesDB.exe", pathServerDir);
        fileOperator.renameFiles(pathServerDir, exeNameQortes, exeNameQortesDB);
        startServer(pathServer, serviceNameQortes, serviceNameQortesDB);
    }

    /**
     * RESTART Server
     */
    public void restartServer(String pathServer, String serviceNameQortes, String serviceNameQortesDB) {
        log.info("pathServer: " + pathServer + " | "
                + "serviceNameQortes: " + serviceNameQortes + " | "
                + "serviceNameQortesDB: " + serviceNameQortesDB);
        stopServer(pathServer, serviceNameQortes, serviceNameQortesDB);
        startServer(pathServer, serviceNameQortes, serviceNameQortesDB);
    }

    /**
     * START Server
     */
    public void startServer(String pathServer, String serviceNameQortes, String serviceNameQortesDB) { //Запуск сервера
        log.info("pathServer: " + pathServer + " | "
                + "serviceNameQortes: " + serviceNameQortes + " | "
                + "serviceNameQortesDB: " + serviceNameQortesDB);

        for (int i = 0; i < 5; i++) { // 5 попыток (50 секунд) на старт компоненты
            serviceOperator.cmdRun(pathServer, serviceNameQortes, " start ");
            serviceOperator.cmdRun(pathServer, serviceNameQortesDB, " start ");
            if (serviceOperator.waitServiceStart(pathServer, serviceNameQortes)) {
                if (serviceOperator.waitServiceStart(pathServer, serviceNameQortesDB)) {
                    optionListController.serverStateNotificator("start");
                    break;
                }
            }
        }
    }

    /**
     * STOP Server
     */
    public void stopServer(String pathServer, String serviceNameQortes, String serviceNameQortesDB) { //Остановка сервера
        log.info("pathServer: " + pathServer + " | "
                + "serviceNameQortes: " + serviceNameQortes + " | "
                + "serviceNameQortesDB: " + serviceNameQortesDB);

        for (int i = 0; i < 5; i++) { // 5 попыток (50 секунд) на старт компоненты
            serviceOperator.cmdRun(pathServer, serviceNameQortes, " stop ");
            serviceOperator.cmdRun(pathServer, serviceNameQortesDB, " stop ");
            if (serviceOperator.waitServiceStop(pathServer, serviceNameQortes)) {
                if (serviceOperator.waitServiceStop(pathServer, serviceNameQortesDB)) {
                    optionListController.serverStateNotificator("stop");
                    break;
                }
            }
        }
    }

    /**
     * GET SERVER VERSION
     * <p>
     * Вернуть ссылка на актульный лог сервера
     */
    public String getServerVersion(String serverName, String pathServer, String infoType) {
        String fileVersion = fileInformator.getFileVersion(pathServer + serverName, infoType);
        log.info(serverName + ": " + fileVersion);
        return fileVersion;
    }
}
