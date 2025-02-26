package com.traderconsole;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FutuTraderConsole {
    private static FutuTraderConsole instance ;
    private boolean isActive = true;
    private FutuMarketDataConnector quoter;
    private FutuTradingConnector tradeConn;
    private static final Logger logger = LogManager.getLogger("FutuTraderConsole");
    private FutuTraderConsole(FutuMarketDataConnector quoter , FutuTradingConnector tradeConn) {
        this.quoter = quoter;
        this.tradeConn = tradeConn;

    }

    private void onInitMktSubscription() {
        logger.info("Subscribing to market data...");
        List<String> future = new ArrayList<>(Arrays.asList(
                "HSImain",
                "HHImain",
                "MHImain"
        ));
        for (String symbol : future) {

            this.quoter.subscribeHKMarket(symbol, QotCommon.SubType.SubType_Ticker);
            //this.quoter.subscribeHKMarket(symbol, QotCommon.SubType.SubType_OrderBook);
        }
    }


    public static FutuTraderConsole getInstance(FutuMarketDataConnector quoter, FutuTradingConnector tradeConn) {
        if (instance == null) {
            instance = new FutuTraderConsole(quoter, tradeConn);
        }
        return instance;
    }

    private void getPositionSnapshot() {
        tradeConn.loadPosition();
        HashMap<String, TrdCommon.Position> book =  tradeConn.getPositionBook();
        for (String symbol : book.keySet()) {
            logger.info("Position: {}", book.get(symbol));
        }
    }

    private void checkHeartbeat() {
        logger.info("Checking service heartbeat...");
        if(!quoter.isHeartbeatValid()){
            logger.fatal("Market data heartbeat is invalid, exiting...");
            isActive = false;
            System.exit(1);
        }
    }
    public void start() {
        logger.info("Starting FutuTraderConsole...");
        quoter.start();
        tradeConn.start();

        onInitMktSubscription();

        while (isActive) {
            try {
                Thread.sleep(1000*5);
                //getPositionSnapshot();
                checkHeartbeat();
                quoter.getSubscriptionInfo();
            } catch (InterruptedException exc) {
                logger.error("Error in main loop", exc);
            }
        }
    }
}
