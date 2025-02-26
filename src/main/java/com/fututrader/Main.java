package com.fututrader;
import com.futu.openapi.FTAPI;
import com.futuconnector.FutuMarketDataConnector;
import com.futuconnector.FutuTradingConnector;
import com.transport.InfluxDBClientManager;
import com.traderconsole.FutuTraderConsole;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Dotenv dotenv = Dotenv.load();
        String influxToken = dotenv.get("INFLUX_TOKEN");
        String org = dotenv.get("ORG");
        String futuTicker = dotenv.get("futu_ticker");
        String futuMktData = dotenv.get("futu_mktdata");
        String influxUrl = dotenv.get("INFLUX_URL");

        String redisUrl = dotenv.get("REDIS_URL");
        int redisPort = Integer.parseInt(dotenv.get("REDIS_PORT"));
        String redisPassword = dotenv.get("REDIS_PASSWORD");

        int futuId = Integer.parseInt(dotenv.get("futu_id"));
        InfluxDBClientManager influxTickerClient = InfluxDBClientManager.getInstance(influxUrl, influxToken, org, futuTicker);
        InfluxDBClientManager influxMktDataClient = InfluxDBClientManager.getInstance(influxUrl, influxToken, org, futuMktData);
        FutuMarketDataConnector marketDataConn = FutuMarketDataConnector.getInstance(influxTickerClient,influxMktDataClient);
        FutuTradingConnector tradeConn = FutuTradingConnector.getInstance(futuId);
        FutuTraderConsole traderConsole = FutuTraderConsole.getInstance(marketDataConn, tradeConn);

        traderConsole.start();


        //marketDataConn.subscribeHKMarket("MHImain", QotCommon.SubType.SubType_OrderBook);
        //marketDataConn.subscribeHKMarket("HSImain", QotCommon.SubType.SubType_OrderBook);


    }
}