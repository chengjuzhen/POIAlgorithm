package com.scc.dataconvert;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;

public class DataConvert {


    public static void main(String[] args) throws ClassNotFoundException, SQLException {

//        friends2txt();
//        stars2txt();
//        photo2txt();
        calculateFd_Tag();//计算fd和tag
        rating2txt();

    }


    protected static Log LOG = LogFactory.getLog(DataConvert.class);


        private static final double EARTH_RADIUS = 6378137;

        //计算两点之间的距离，返回单位为 m
        public static double GetDistance(double longitude1, double latitude1, double longitude2,
                                         double latitude2) {
            double radLat1 = rad(latitude1);
            double radLat2 = rad(latitude2);
            double a = radLat1 - radLat2;
            double b = rad(longitude1) - rad(longitude2);
            double s =
                    2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1)
                            * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 10000) / 10000;
            return s;
        }

        private static double rad(double d) {
            return d * Math.PI / 180.0;
        }


    public static void calculateFd_Tag() throws SQLException, ClassNotFoundException{
        Connection con;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        StringBuffer sb = new StringBuffer();

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed())
                LOG.info(System.currentTimeMillis()+"Succeeded connecting to the Database(10.21.76.120)!");

            LOG.info("开始 查询 new_user 表记录!");

            String all_user = "select id from new_user ";
            PreparedStatement ps = con.prepareStatement(all_user);
            ResultSet rs_user = ps.executeQuery(all_user);
            LOG.info("成功查询到new_user表所有user_id记录!");


            //获得所有用户列表
            List<String> userList = new ArrayList<String>();
            while(rs_user.next()){
                String user_id = rs_user.getString("id");
                userList.add(user_id);
            }

            rs_user.close();
            ps.close();

            //获得所有business对应的经纬度
            Map businessMap = new HashMap<String, Object[]>();


            LOG.info("开始 查询 new_business 表记录!");
            String all_business = "select id,latitude,longitude,category from new_business,category where id=business_id ";
            ps = con.prepareStatement(all_business);
            ResultSet rs_business = ps.executeQuery(all_business);
            LOG.info("成功查询到 new_business 表所有 id,latitude,longitude 记录!");

            while (rs_business.next()){
                String business_id = rs_business.getString("id");
                float latitude = rs_business.getFloat("latitude");
                float longitude = rs_business.getFloat("longitude");
                String category = rs_business.getString("category");
                Object[] ll = new Object[3];
                ll[0] = latitude;
                ll[1] = longitude;
                ll[2] = category;
                LOG.info("装包："+ll[0]+"\t"+ll[1]+"\t"+ll[2]+"\n");
                if(!businessMap.containsKey(business_id)){
                    businessMap.put(business_id, ll);
                }


            }

            rs_business.close();
            ps.close();

            //计算fd和tag
            List<Object[]> fd_tag_list = new ArrayList<Object[]>();


            for(String user_id : userList){
//                LOG.info("开始 查询 review 表记录!");
                String all_review = "select business_id from new_review where user_id='"+ user_id + "'";
                ps = con.prepareStatement(all_review);
                ResultSet rs_review = ps.executeQuery(all_review);
//                LOG.info("成功查询到 review 表所有 business_id 记录!");

                //按照user_id获取business_id列表
                List<String> businessList = new ArrayList<String>();
                float latitude_all = 0;
                float longitude_all = 0;
                Map<String, Integer> categories = new HashMap<String, Integer>();//统计每个business对应的tag数量

                while(rs_review.next()){
                    String business_id = rs_review.getString("business_id");
                    businessList.add(business_id);
                    Object[] ll = (Object[]) businessMap.get(business_id);

                    LOG.info("拆包："+ll[0]+"\t"+ll[1]+"\t"+ll[2]+"\n");
                    latitude_all += (Float) ll[0];
                    longitude_all += (Float) ll[1];
                    String tag = (String) ll[2];
                    //TODO 临时修改
                    if(categories.containsKey(tag)){
                        categories.put(tag, categories.get(tag) + 1);
                    }else {
                        categories.put(tag, 1);
                    }
                }

                //计算 fd,tag
                int size = businessList.size();
                float latitude_o = latitude_all / size;
                float longitude_o = longitude_all / size;


                for(String business : businessList){
                    Object[] ll = (Object[]) businessMap.get(business);
                    float ll_1 = (Float) ll[0];
                    float ll_2 = (Float) ll[1];
                    //计算距离 d
                    double d = GetDistance(ll_1, ll_2, latitude_o, longitude_o);

                    //fd = ln(d) OR fd = a * d^b
                    //暂取，fd = 1000 * d ^ -1
                    double fd = 1.0;

                    if(d != 0.0){
                        fd = 10 * Math.pow(d, -1);
                    }

                    String category = (String) ll[2];

                    double tag = categories.get(category) * 1.0 / size * 1.0;

                    Object[] line = new Object[4];
                    line[0] = business;
                    line[1] = user_id;
                    line[2] = fd;
                    line[3] = tag;

                    fd_tag_list.add(line);

                }

                rs_review.close();
                ps.close();

            }

            //根据 user_id 和 business_id 更新数据表字段 fd , tag
            for(Object[] line : fd_tag_list){
                String business_id = (String)line[0];
                String user_id = (String)line[1];
                double fd = (Double)line[2];
                double tag = (Double)line[3];

                String update_review = "update new_review set fd="+ fd +", tag="+ tag +" where business_id='"+business_id
                        +"' and user_id='" +user_id+"'";
                ps = con.prepareStatement(update_review);
                ps.executeUpdate(update_review);

                ps.close();

            }

            String cal = "UPDATE new_review set rating = (stars*fd*tag) ";
            ps = con.prepareStatement(cal);
            ps.executeUpdate(cal);

            ps.close();

        } catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {
            e.printStackTrace();
            LOG.error("JDBC有错误!");

        }

    }


    public static void photo2txt() throws SQLException, ClassNotFoundException{

        Connection con;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        String output = "/home/cc01/data/poi/photo/poi_photo_new.txt"; ///home/cc01/data/poi/photo/poi_photo_test.txt
        StringBuffer sb = new StringBuffer();

        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed())
                LOG.info(System.currentTimeMillis()+"Succeeded connecting to the Database(10.21.76.120)!");

            LOG.info("开始 查询 new_photo 表记录!");

            String all_business = "select business_id from new_photo group by business_id ";
            PreparedStatement ps = con.prepareStatement(all_business);
            ResultSet rs_business = ps.executeQuery(all_business);
            LOG.info("成功查询到new_photo表所有business_id记录!");
            StringBuffer sb_business = new StringBuffer();
            while(rs_business.next()){
                String business_id = rs_business.getString("business_id");
                sb_business.append(business_id);
                sb_business.append(",");
            }

            all_business = null;
            rs_business.close();
            ps.close();

            rs_business = null;

            String[] bus_arr = sb_business.toString().split(",");

            for(String bus : bus_arr){
                String all_photo = "select extractor from new_photo where business_id = '"+ bus + "'";
                ps = con.prepareStatement(all_photo);
                ResultSet rs_photo = ps.executeQuery(all_photo);
                StringBuffer meanExtra = new StringBuffer();//以逗号分隔

                double[] amount = new double[1000];
                int count = 0;
                while (rs_photo.next()){
                    String extractor = rs_photo.getString("extractor");
                    String[] exTmp = extractor.split("[ \\[\\]\n\t\r]+");

                    //转换成double数组
                    double[] tmparr = new double[1000];
                    for(int i=1; i<exTmp.length; i++){
                        tmparr[i-1] = Double.parseDouble(exTmp[i]);
                    }

                    //计算总和
                    for(int i=0; i<amount.length; i++){
                        amount[i] += tmparr[i];
                    }
                    count++;
//                    LOG.info("计算    business_id:"+bus+"\t的数量："+count);
                }

                LOG.info("business_id:"+bus+"\t的数量："+count+", 计算完成！");

                //计算均值，并转换成以逗号分隔的字符串

                for(int i=0; i<amount.length; i++){
                    amount[i] = amount[i] / count;
                    meanExtra.append(amount[i]).append(",");
                }

                sb.append(bus).append("\t").append(meanExtra.toString()).append("\n");

//                LOG.info("business_id:"+bus+"\t的均值为："+meanExtra.toString());


                rs_photo.close();
                ps.close();
                rs_photo = null;
                all_photo = null;

                amount = null;

            }

            LOG.info("成功将记录写到缓存区!");

            con.close();

        }catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {
            e.printStackTrace();
            LOG.error("JDBC有错误!");

        }


        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(new File(output));
            bw = new BufferedWriter(fw);

//            System.out.println(sb.toString());
            bw.write(sb.toString());
            bw.flush();
            LOG.info("成功写入文件!");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fw.close();
                bw.close();

            }catch (Exception e1){

            }
        }


    }



    public static void rating2txt() throws SQLException, ClassNotFoundException{

        Connection con;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        String output = "/home/cc01/data/poi/rating/poi_rating_new.txt";
        StringBuffer sb = new StringBuffer();

        DecimalFormat df=(DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(4);

        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed())
                LOG.info(System.currentTimeMillis()+"Succeeded connecting to the Database(10.21.76.120)!");
            Statement statement = con.createStatement();

            LOG.info("开始 查询 new_review 表记录!");

            String sql = "select user_id, business_id, rating  from new_review ";
            ResultSet rs = statement.executeQuery(sql);

            LOG.info("成功查询到 new_review 表所有记录!");

            String userId = null;
            String itemId = null;
            double rating = 0;

            while(rs.next()){
                userId = rs.getString("user_id");
                itemId = rs.getString("business_id");
                rating = rs.getDouble("rating");
                sb.append(userId).append("\t").append(itemId).append("\t").append(df.format(rating)).append("\n");
            }
            LOG.info("成功将记录写到缓存区!");

        }catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {

        }


        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(new File(output));
            bw = new BufferedWriter(fw);

//            System.out.println(sb.toString());
            bw.write(sb.toString());
            bw.flush();
            LOG.info("成功写入文件!");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fw.close();
                bw.close();

            }catch (Exception e1){

            }
        }


    }

    public static void stars2txt() throws SQLException, ClassNotFoundException{

        Connection con;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        String output = "/home/cc01/data/poi/rating/poi_stars_new.txt";
        StringBuffer sb = new StringBuffer();

        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed())
                LOG.info(System.currentTimeMillis()+"Succeeded connecting to the Database(10.21.76.120)!");
            Statement statement = con.createStatement();

            LOG.info("开始 查询 new_review 表记录!");

            String sql = "select user_id, business_id, stars  from new_review ";
            ResultSet rs = statement.executeQuery(sql);

            LOG.info("成功查询到 new_review 表所有记录!");

            String userId = null;
            String itemId = null;
            int rating = 0;

            while(rs.next()){
                userId = rs.getString("user_id");
                itemId = rs.getString("business_id");
                rating = rs.getInt("stars");
                sb.append(userId).append("\t").append(itemId).append("\t").append(rating).append("\n");
            }
            LOG.info("成功将记录写到缓存区!");

        }catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {

        }


        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(new File(output));
            bw = new BufferedWriter(fw);

//            System.out.println(sb.toString());
            bw.write(sb.toString());
            bw.flush();
            LOG.info("成功写入文件!");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fw.close();
                bw.close();

            }catch (Exception e1){

            }
        }


    }

    public static void friends2txt() throws SQLException, ClassNotFoundException{

        Connection con;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        String output = "E:/essayData/data/poi/trust/poi_trust_new.txt";
        StringBuffer sb = new StringBuffer();

        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed())
                LOG.info("Succeeded connecting to the Database(10.21.76.120)!");
            Statement statement = con.createStatement();

            LOG.info("开始 查询 new_friend 表记录!");

            String sql = "select * from new_friend ";
            ResultSet rs = statement.executeQuery(sql);

            LOG.info("成功查询到 new_friend 表所有记录!");

            String userId = null;
            String friendId = null;

            while(rs.next()){
                userId = rs.getString("user_id");
                friendId = rs.getString("friend_id");
                sb.append(userId).append("\t").append(friendId).append("\n");
            }
            LOG.info("成功将记录写到缓存区!");

        }catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {

        }


        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(new File(output));
            bw = new BufferedWriter(fw);

//            System.out.println(sb.toString());
            bw.write(sb.toString());
            bw.flush();
            LOG.info("成功写入文件!");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fw.close();
                bw.close();

            }catch (Exception e1){

            }
        }


    }


    public static void sqlTest(){

        Connection con;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://10.21.76.120:3306/yelp_db";

        String user = "root";
        String password = "111111";

        String output = "/home/cc01/test.txt";//"/home/cc01/test.txt";// /home/cc01/test.txt
        StringBuffer sb = new StringBuffer();

        try{
            Class.forName(driver);
            con = DriverManager.getConnection(url,user,password);

            if(!con.isClosed()) {
                LOG.info("Succeeded connecting to the Database(10.21.76.120)!");
            }
            Statement statement = con.createStatement();


            String sql = "select count(*) as num from new_friend ";
            ResultSet rs = statement.executeQuery(sql);

            int count = 0;

            while(rs.next()){
                count = rs.getInt("num");
                LOG.info("返回结果集！ return select collection!");
                sb.append(count);
            }

        }catch(ClassNotFoundException e){
            LOG.error("找不到 mysql 驱动类!");

        }catch(SQLException e){
            LOG.error("mysql 连接失败!");

        }catch (Exception e) {

        }


        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(new File(output));
            bw = new BufferedWriter(fw);

            LOG.info("准备写入文件！ ready to write into file!");

//            System.out.println(sb.toString());
            bw.write(sb.toString());
            bw.flush();
            LOG.info("写入文件完毕！had written into file!");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fw.close();
                bw.close();

            }catch (Exception e1){

            }
        }


    }


}
