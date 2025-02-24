package com.fututrader;
import com.futu.openapi.FTAPI;
import com.futu.openapi.pb.QotCommon;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;
import com.transport.InfluxDBClientManager;
import com.traderconsole.FutuTraderConsole;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        FTAPI.init();
        Dotenv dotenv = Dotenv.load();
        String influxToken = dotenv.get("INFLUX_TOKEN");
        String org = dotenv.get("ORG");
        String futuTicker = dotenv.get("futu_ticker");
        String futuMktData = dotenv.get("futu_mktdata");
        String influxUrl = dotenv.get("INFLUX_URL");
        int futuId = Integer.parseInt(dotenv.get("futu_id"));
        InfluxDBClientManager influxTickerClient = InfluxDBClientManager.getInstance(influxUrl, influxToken, org, futuTicker);
        InfluxDBClientManager influxMktDataClient = InfluxDBClientManager.getInstance(influxUrl, influxToken, org, futuMktData);
        FutuMarketDataConnector marketDataConn = new FutuMarketDataConnector(influxTickerClient,influxMktDataClient);
        FutuTradingConnector tradeConn = FutuTradingConnector.getInstance(futuId);
        FutuTraderConsole traderConsole = FutuTraderConsole.getInstance(marketDataConn, tradeConn);

        traderConsole.start();

        if (marketDataConn.isReady()) {
            System.out.println("Market Data Connector is active");
        }
        //marketDataConn.subscribeHKMarket("MHImain", QotCommon.SubType.SubType_OrderBook);
        //marketDataConn.subscribeHKMarket("HSImain", QotCommon.SubType.SubType_OrderBook);

        while (true) {
            try {
                Thread.sleep(1000*5);
                //tradingConn.loadPosition();
                marketDataConn.getSubscriptionInfo();
            } catch (InterruptedException exc) {

            }
        }
    }
}