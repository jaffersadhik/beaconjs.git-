package com.itextos.beacon.commonlib.constants;

import java.util.ArrayList;
import java.util.List;

public class Enumtest
{

    public static void main(
            String[] args)
    {
        final List<TempThread> threads = new ArrayList<>();

        for (int index = 1; index < 100; index++)
            threads.add(new TempThread());

        for (final TempThread tt : threads)
        {
            tt.start();
            System.out.println(tt.ct);
        }
    }

}

class TempThread
        extends
        Thread
{

    ClusterType ct = null;

    @Override
    public void run()
    {
        ct = ClusterType.getCluster("bulk");
        System.out.println(">>>>>>>>>>>>>>>>> " + ct);
    }

}