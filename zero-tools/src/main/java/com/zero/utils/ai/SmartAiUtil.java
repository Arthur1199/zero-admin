//package com.zero.utils.ai;
//
//import cn.smartjavaai.common.entity.R;
//import cn.smartjavaai.common.enums.DeviceEnum;
//import cn.smartjavaai.face.config.FaceDetConfig;
//import cn.smartjavaai.face.config.FaceRecConfig;
//import cn.smartjavaai.face.constant.FaceDetectConstant;
//import cn.smartjavaai.face.enums.FaceDetModelEnum;
//import cn.smartjavaai.face.enums.FaceRecModelEnum;
//import cn.smartjavaai.face.factory.FaceDetModelFactory;
//import cn.smartjavaai.face.factory.FaceRecModelFactory;
//import cn.smartjavaai.face.model.facedect.FaceDetModel;
//import cn.smartjavaai.face.model.facerec.FaceRecModel;
//import com.alibaba.fastjson2.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 作者: Arthur 亚瑟
// * 时间: 2025/7/7 23:09
// * 说明:
// */
//public class SmartAiUtil {
//
//    private static final Logger log = LoggerFactory.getLogger(SmartAiUtil.class);
//
//    //设备类型
//    public static DeviceEnum device = DeviceEnum.CPU;
//
//    /**
//     *
//     * 作者: Arthur 亚瑟
//     * 时间: 2025/7/7 23:10
//     * @return
//     * 返回值:FaceDetModel
//     * 返回值说明：获取人脸检测模型
//     */
//    public static FaceDetModel getFaceDetModel(){
//        FaceDetConfig config = new FaceDetConfig();
//        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);//人脸检测模型
//        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);//只返回相似度大于该值的人脸
//        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);//用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
//        config.setDevice(device);
//        return FaceDetModelFactory.getInstance().getModel(config);
//    }
//
//    /**
//     * 获取人脸识别模型
//     * @return
//     */
//    public static FaceRecModel getFaceRecModel(){
//        FaceRecConfig config = new FaceRecConfig();
//        config.setModelEnum(FaceRecModelEnum.FACENET_MODEL);
////        config.setModelPath("/Users/xxx/Documents/develop/model/elasticface.pt");
////        config.setModelPath("/Users/xxx/Documents/develop/model/InsightFace/model_mobilefacenet.pt");
//        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
//        config.setCropFace(true);
//        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
//        config.setAlign(true);
//        config.setDevice(device);
//        //指定人脸检测模型
//        config.setDetectModel(getFaceDetModel());
//        return FaceRecModelFactory.getInstance().getModel(config);
//    }
//
//    public static void main(String[] args) {
//        try {
//            FaceRecModel faceRecModel = getFaceRecModel();
//            //基于图像直接比对人脸特征
//            R<Float> similarResult = faceRecModel.featureComparison("C:\\Users\\Administrator\\Desktop\\imgFile\\iu_1.jpg","C:\\Users\\Administrator\\Desktop\\imgFile\\iu_2.jpg");
//            if(similarResult.isSuccess()){
//                //相似度阈值不同模型不同，具体参看文档
//                log.info("人脸比对相似度：{}", JSONObject.toJSONString(similarResult.getData()));
//            }else{
//                log.info("人脸比对失败：{}", similarResult.getMessage());
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//}
