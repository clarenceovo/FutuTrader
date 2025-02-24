package com.fututrader;
import com.futu.openapi.FTAPI;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        FTAPI.init();
        FutuTradingConnector tradingConn = FutuTradingConnector.getInstance();
        tradingConn.start();

        while (true) {
            try {
                Thread.sleep(1000*5);
                tradingConn.loadPosition();
                Thread.sleep(1000);
            } catch (InterruptedException exc) {

            }
        }
    }
}