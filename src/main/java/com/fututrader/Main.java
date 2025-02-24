package com.fututrader;
import com.futu.openapi.FTAPI;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        FTAPI.init();
        FutuTradingConnector tradingConn = new FutuTradingConnector();
        tradingConn.start();

        while (true) {
            try {
                Thread.sleep(1000 * 600);
            } catch (InterruptedException exc) {

            }
        }
    }
}