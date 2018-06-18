package com.sccJar.jar;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static java.lang.Double.parseDouble;
import static java.lang.Math.round;

public class TestJar {

    /**
     * LOG
     */
    protected static Log LOG = LogFactory.getLog(TestJar.class);

    public static void doSomething(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LOG.info("当前时间是: " + dateFormat.format(new Date()) );
//        System.out.println("当前时间是" + dateFormat.format(new Date()));
    }

    public static void main(String[] args) {

        double a  = round(parseDouble("59.591234564651564545645") * 100) / 100.0;


        System.out.println(a);
        //        doSomething();
//
//        LOG.trace("我是trace信息");
//        LOG.debug("我是debug信息");
//        LOG.info("我是info信息");
//        LOG.warn("我是warn信息");
//        LOG.error("我是error信息");
//        LOG.fatal("我是fatal信息");
    }
}
