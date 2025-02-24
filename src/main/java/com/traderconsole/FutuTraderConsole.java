package com.traderconsole;

import com.futu.openapi.pb.QotCommon;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
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
                "MHImain",
                "HTImain",
                "HHImain"
        ));
        for (String symbol : future) {
            this.quoter.subscribeHKMarket(symbol, QotCommon.SubType.SubType_Ticker);
            this.quoter.subscribeHKMarket(symbol, QotCommon.SubType.SubType_OrderBook);
        }
    }


    public static FutuTraderConsole getInstance(FutuMarketDataConnector quoter, FutuTradingConnector tradeConn) {
        if (instance == null) {
            instance = new FutuTraderConsole(quoter, tradeConn);
        }
        return instance;
    }

    public void start() {
        quoter.start();
        tradeConn.start();
        if (quoter.isReady()){
            onInitMktSubscription();
        }
        while (isActive) {
            try {
                Thread.sleep(1000*5);
                tradeConn.loadPosition();
                //marketDataConn.getSubscriptionInfo();
            } catch (InterruptedException exc) {
                logger.error("Error in main loop", exc);
            }
        }
    }
}
