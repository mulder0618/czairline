package com.czairline.index.controller;


import com.czairline.net.sourceforge.tess4j.Tesseract;
import com.czairline.test.Tesseract1Test;
import com.czairline.utils.HttpRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mulder on 2016/2/14.
 */
@Controller
public class IndexController {

 static  CookieStore cookieStore;

    @RequestMapping("/index")
    public ModelAndView showIndex(
            ModelAndView modelAndView,
            HttpServletResponse resp,
            HttpServletRequest r
    ) throws Exception {
     /*   String indexResult = HttpRequest.sendGet("http://cabin.csair.com/login.jsp",null);
        modelAndView.addObject("indexResult",indexResult);*/
   /*
        String urlLogin = "http://cabin.csair.com/login.jsp";

        // 登录成功后想要访问的页面 可以是下载资源 需要替换成自己的iteye Blog地址
        String urlAfter = "http://cabin.csair.com/authImg";

        DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());

        *//**
         * 第一次请求登录页面 获得cookie
         * 相当于在登录页面点击登录，此处在URL中 构造参数，
         * 如果参数列表相当多的话可以使用HttpClient的方式构造参数
         * 此处不赘述
         *//*
        HttpPost post = new HttpPost(urlLogin);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        cookieStore = client.getCookieStore();
        client.setCookieStore(cookieStore);

        *//**
         * 带着登录过的cookie请求下一个页面，可以是需要登录才能下载的url
         * 此处使用的是iteye的博客首页，如果登录成功，那么首页会显示【欢迎XXXX】
         *
         *//*
        HttpGet get = new HttpGet(urlAfter);
        response = client.execute(get);
        entity = response.getEntity();
        InputStream inStream = entity.getContent();

        //生成图片文件
        ImageIO.write(ImageIO.read(inStream), "JPG", new File("C:\\Users\\mulder\\JavaProjects\\czairline\\authImg6.jpg"));
        //------------
        Tesseract1Test t = new Tesseract1Test();
        t.handleImg("C:\\Users\\mulder\\JavaProjects\\czairline\\authImg6.jpg");

        //读取处理过后的图片
        File imageFile = new File("C:\\Users\\mulder\\JavaProjects\\czairline\\czauthImg.jpg");
        Tesseract instance = Tesseract.getInstance(); // JNA Interface Mapping
        instance.setDatapath("tessdata");
        String result = instance.doOCR(imageFile);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        modelAndView.addObject("codeResult",result);*/
        modelAndView.setViewName("index/index");
        return modelAndView;
    }

    @RequestMapping("/login")
    public ModelAndView login(
            ModelAndView modelAndView,
            HttpServletRequest request,
            String verCode
    ) throws IOException {
      /*  String workResult = HttpRequest.sendGet("http://cabin.csair.com/j_spring_security_check","radio=crew&j_username=735685&j_password=gsy18610632097$&j_code="+verCode.trim());*/

        String urlAfter = "http://cabin.csair.com/j_spring_security_check?radio=crew&j_username=735685&j_password=gsy18610632097$&j_code="+verCode.trim();
        DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());
        client.setCookieStore(cookieStore);
        HttpGet get = new HttpGet(urlAfter);
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        //System.out.println("Response content: " + EntityUtils.toString(entity, "UTF-8"));
        String result = EntityUtils.toString(entity, "UTF-8");

        //获取日程安排
        String workUrl = "http://cabin.csair.com/newportal/cabin/homepage-CabinHomePage-loadFlyTask.do";
        HttpGet getwork = new HttpGet(workUrl);
        HttpResponse responsework = client.execute(getwork);
        HttpEntity entitywork = responsework.getEntity();
        String resultwork = EntityUtils.toString(entitywork, "UTF-8");


        //截取机组人员信息------------------
        String txt = resultwork;
        ArrayList jizurenyunlist = new ArrayList();
        while (true){
            int firstIndex = txt.indexOf("showFlyOther(");
            if(firstIndex==-1){
                break;
            }
            String newTxt = "";
            newTxt = txt.substring(firstIndex+13);
            int lastIndex = newTxt.indexOf("></div>");
            newTxt = newTxt.substring(0,lastIndex-2);
            jizurenyunlist.add(newTxt);
            //删除查找
            txt = txt.substring(firstIndex+12);
        }

        //System.out.println(jizurenyunlist);
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<jizurenyunlist.size();i++){
            String jizhu = jizurenyunlist.get(i).toString();
            String[] jizutmp = jizhu.split(",");
            String actStrDtTmGmt = jizutmp[4].replaceAll("'","");
            String fltNum = jizutmp[1].replaceAll("'","");
            String portA = jizutmp[2].replaceAll("'","");
            String portB = jizutmp[3].replaceAll("'","");
            String seriesNum = jizutmp[5].replaceAll("'","");

            String paramUrl = "actStrDtTmGmt="+actStrDtTmGmt+"&fltNum="+fltNum+"&portA="+portA+"&portB="+portB+"&seriesNum="+seriesNum;
            // System.out.println(actStrDtTmGmt);
           // System.out.println(seriesNum);

            //获取机组人员
            String jizuUrl = "http://cabin.csair.com/newportal/cabin/scheduleplan-SchedulePlan-showCrewInfo.do";
            HttpPost httpPost = new HttpPost(jizuUrl);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("actStrDtTmGmt", actStrDtTmGmt));
            nvps.add(new BasicNameValuePair("fltNum", fltNum));
            nvps.add(new BasicNameValuePair("portA", portA));
            nvps.add(new BasicNameValuePair("portB", portB));
            nvps.add(new BasicNameValuePair("seriesNum", seriesNum));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse responsejizu = client.execute(httpPost);
            HttpEntity entityjizu = responsejizu.getEntity();
            String resultjizu = EntityUtils.toString(entityjizu, "UTF-8");
            System.out.println(resultjizu);


            sb.append(resultjizu);
            sb.append("<br/><br/><br/>");
        }



        //System.out.println(jizurenyunlist);

        modelAndView.addObject("workResult", resultwork);
        modelAndView.addObject("jizuResult", sb.toString());
        modelAndView.setViewName("index/work");
        return modelAndView;
    }


    @RequestMapping(value = "/authImg" , produces = "image/jpg; charset=utf-8")
    public void getvercode(HttpServletResponse resp,HttpServletRequest r) throws Exception {

        // 需要提交登录的信息
        String urlLogin = "http://cabin.csair.com/login.jsp";

        // 登录成功后想要访问的页面 可以是下载资源 需要替换成自己的iteye Blog地址
        String urlAfter = "http://cabin.csair.com/authImg";

        DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());

        HttpPost post = new HttpPost(urlLogin);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        cookieStore = client.getCookieStore();
        client.setCookieStore(cookieStore);

        HttpGet get = new HttpGet(urlAfter);
        response = client.execute(get);
        entity = response.getEntity();
        InputStream inStream = entity.getContent();

        byte data[] = readInputStream(inStream);
        inStream.read(data);  //读数据


        inStream.close();
        resp.setContentType("image/jpg"); //设置返回的文件类型
        OutputStream os = resp.getOutputStream();
        os.write(data);
        os.flush();
        os.close();


    }

    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        //inStream.close();
        return outStream.toByteArray();
    }

}
