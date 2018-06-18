package com.scc.algorithm;

import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.filter.GenericRecommendedFilter;
import net.librec.filter.RecommendedFilter;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ItemKNNRecommender;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.cf.rating.PMFRecommender;
import net.librec.recommender.context.rating.*;
import net.librec.recommender.item.RecommendedItem;
import net.librec.similarity.PCCSimilarity;
import net.librec.similarity.RecommenderSimilarity;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmTest {

    protected static Log LOG = LogFactory.getLog(AlgorithmTest.class);


    public static void main(String[] args) throws Exception{

        //ratio
        String ratio1 = "0.99";
        String ratio2 = "0.80";
        String ratio3 = "0.50";
        String ratio4 = "0.20";


        //dimension
        String dimension1 = "5";
        String dimension2 = "10";


        //SVPOI
//        svpoi(ratio1, dimension1);
//        svpoi(ratio1, dimension2);
//        svpoi(ratio2, dimension1);
//        svpoi(ratio2, dimension2);
//        svpoi(ratio3, dimension1);
//        svpoi(ratio3, dimension2);
//        svpoi(ratio4, dimension1);
//        svpoi(ratio4, dimension2);

        //SVPOI2
        svpoi2(ratio1, dimension1);
        svpoi2(ratio1, dimension2);
        svpoi2(ratio2, dimension1);
        svpoi2(ratio2, dimension2);
        svpoi2(ratio3, dimension1);
        svpoi2(ratio3, dimension2);
        svpoi2(ratio4, dimension1);
        svpoi2(ratio4, dimension2);


        //sorec
//        soRec(ratio1, dimension1);
//        soRec(ratio1, dimension2);
//        soRec(ratio2, dimension1);
//        soRec(ratio2, dimension2);
//        soRec(ratio3, dimension1);
//        soRec(ratio3, dimension2);
//        soRec(ratio4, dimension1);
//        soRec(ratio4, dimension2);

        //pmf
//        pmf(ratio1, dimension1);
//        pmf(ratio1, dimension2);
//        pmf(ratio2, dimension1);
//        pmf(ratio2, dimension2);
//        pmf(ratio3, dimension1);
//        pmf(ratio3, dimension2);
//        pmf(ratio4, dimension1);
//        pmf(ratio4, dimension2);

        //trustSVD
//        trustSVD(ratio1, dimension1);
//        trustSVD(ratio1, dimension2);
//        trustSVD(ratio2, dimension1);
//        trustSVD(ratio2, dimension2);
//        trustSVD(ratio3, dimension1);
//        trustSVD(ratio3, dimension2);
//        trustSVD(ratio4, dimension1);
//        trustSVD(ratio4, dimension2);

        //trustMF
//        trustMF(ratio1, dimension1);
//        trustMF(ratio1, dimension2);
//        trustMF(ratio2, dimension1);
//        trustMF(ratio2, dimension2);
//        trustMF(ratio3, dimension1);
//        trustMF(ratio3, dimension2);
//        trustMF(ratio4, dimension1);
//        trustMF(ratio4, dimension2);



    }

    public static void trustMF(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/trustmf-test.properties");
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("data.appender.path", "poi/trust"); //  poi/trust   //filmtrust/trust
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        // training
        Recommender recommender = new TrustMFRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        LOG.info("mae:"+mae);

        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);
        LOG.info("rmse:"+rmse);


        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-trustmf-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public static void trustSVD(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/trustsvd-test.properties");
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("data.appender.path", "poi/trust"); //  poi/trust   //filmtrust/trust
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        // training
        Recommender recommender = new TrustSVDRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);


        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-trustsvd-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static void pmf(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/pmf-test.properties");
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        // training
        Recommender recommender = new PMFRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);


        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-pmf-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void soRec(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/sorec-test.properties");
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("data.appender.path", "poi/trust"); //  poi/trust   //filmtrust/trust
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build similarity
//        conf.set("rec.recommender.similarity.key" ,"item");
//        RecommenderSimilarity similarity = new PCCSimilarity();//有好多相似度度量公式
//        similarity.buildSimilarityMatrix(dataModel);
//        context.setSimilarity(similarity);

        // build recommender
        // training
        Recommender recommender = new SoRecRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);


        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-sorec-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void svpoi(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/svpoi-test.properties");//注意修改配置文件
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("data.appender.path", "poi/trust"); //  poi/trust   //filmtrust/trust
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        // training
        Recommender recommender = new SVPOIRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);

        //这个地方注意修改
        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-svpoi-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void svpoi2(String ratio, String dimension) throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "/home/cc01/data");// /home/cc01/data  //E:/essayData/data
        conf.set("dfs.result.dir", "/home/cc01/result");// /home/cc01/result  //E:/essayData/result
        conf.set("data.input.path", "poi/rating"); //"poi/rating"  //filmtrust/rating

        //data.splitter.trainset.ratio=0.8
        conf.set("data.splitter.trainset.ratio", ratio);


        Configuration.Resource resource = new Configuration.Resource("rating/svpoi2-test.properties");//注意修改配置文件
        conf.addResource(resource);


        //rec.factor.number
        conf.set("rec.factor.number", dimension);
        //data.appender.path
        conf.set("data.appender.path", "poi/trust"); //  poi/trust   //filmtrust/trust
        conf.set("rec.iterator.maximum", "2000"); //  2000


        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        // training
        Recommender recommender = new SVPOI2Recommender();//注意修改此处！
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        double mae = recommender.evaluate(evaluator);
        System.out.println("mae:"+mae);
        RecommenderEvaluator evaluator2 = new RMSEEvaluator();
        double rmse = recommender.evaluate(evaluator2);
        System.out.println("rmse:"+rmse);

        //这个地方注意修改！
        String evaluatorOutput = conf.get("dfs.result.dir") + "/" + conf.get("data.input.path") + "-svpoi2-output/mae-rmse-"+ratio+"-"+dimension+".txt";
        try {
            FileUtil.writeString(evaluatorOutput, "mae:"+mae+"\nrmse:"+rmse);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void algorithm1() throws Exception {

        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "E:/essayData/data");// /home/cc01/data
        Configuration.Resource resource = new Configuration.Resource("rec/cf/userknn-test.properties");
        conf.addResource(resource);

        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        // training
        Recommender recommender = new UserKNNRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        recommender.evaluate(evaluator);

        // recommendation results
        List recommendedItemList = recommender.getRecommendedList();
        RecommendedFilter filter = new GenericRecommendedFilter();
        recommendedItemList = filter.filter(recommendedItemList);

        System.out.println();
    }



    public static void algorithm2() throws Exception {

        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", "E:/essayData/data");// /home/cc01/data
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build similarity
        conf.set("rec.recommender.similarity.key" ,"item");
        RecommenderSimilarity similarity = new PCCSimilarity();
        similarity.buildSimilarityMatrix(dataModel);
        context.setSimilarity(similarity);

        // build recommender
        conf.set("rec.neighbors.knn.number", "5");
        Recommender recommender = new ItemKNNRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        recommender.recommend(context);

        // evaluate the recommended result
        RecommenderEvaluator evaluator = new RMSEEvaluator();
        System.out.println("RMSE:" + recommender.evaluate(evaluator));

        // set id list of filter
        List<String> userIdList = new ArrayList<String>();
        List<String> itemIdList = new ArrayList<String>();
        userIdList.add("1");
        itemIdList.add("70");

        // filter the recommended result
        List<RecommendedItem> recommendedItemList = recommender.getRecommendedList();
        GenericRecommendedFilter filter = new GenericRecommendedFilter();
        filter.setUserIdList(userIdList);
        filter.setItemIdList(itemIdList);
        recommendedItemList = filter.filter(recommendedItemList);

        // print filter result
        for (RecommendedItem recommendedItem : recommendedItemList) {
            System.out.println(
                    "user:" + recommendedItem.getUserId() + " " +
                            "item:" + recommendedItem.getItemId() + " " +
                            "value:" + recommendedItem.getValue()
            );
        }
    }

}
