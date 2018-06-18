package com.tecent.dataconvert;

import java.io.*;

public class DataConvert {


    public static void main(String[] args) throws Exception {
        copy2file();

    }

    public static void copy2file() throws Exception {

        BufferedReader bf = new BufferedReader(new FileReader("F:/preliminary_contest_data/userFeature.data"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("F:/preliminary_contest_data/userFeature.data.1000.line"));

        BufferedWriter bw2 = new BufferedWriter(new FileWriter("F:/preliminary_contest_data/userFeature.data.amount"));

        String str = "";
        int i = 0;
        StringBuffer sb = new StringBuffer();

        while ( (str = bf.readLine()) != null  && i<1000){
            sb.append(str);
            sb.append("\n");
            i++;
        }

        String str2 = "";
        int i2 = 0;

        while ((str2 = bf.readLine()) != null ){
            i++;

        }

        bw.write(sb.toString());
        bw2.write(""+i);

        bw.flush();
        bw2.flush();

        bw.close();
        bw2.close();

    }





}
