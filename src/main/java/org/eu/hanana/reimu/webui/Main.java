package org.eu.hanana.reimu.webui;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements Runnable{
    private static final Logger log = LogManager.getLogger(Main.class);
    public static Main main;
    public String[] args;
    public WebUi webUi;

    public static void main(String[] args) {
        System.out.println("Hello world!");
        main=new Main();
        main.args=args;
        main.run();
    }

    @SneakyThrows
    @Override
    public void run() {
        webUi = new WebUi("0.0.0.0",5160);
        webUi.open(true);
        while (webUi.isRunning()) {
            Thread.sleep(100);
        }
        log.info("Exited!");
    }
}