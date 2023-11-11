package com.itextos.beacon.platform.payload;

public class StartApplication
{

    public static void main(
            String[] args)
    {
        Prometheus.registerServer();
        Prometheus.registerMetrics();

        final PayloadDataReader lDataReader = new PayloadDataReader();
    }

}