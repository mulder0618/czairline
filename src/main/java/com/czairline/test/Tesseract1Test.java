/**
 * Copyright @ 2010 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.czairline.test;

import com.czairline.net.sourceforge.jdeskew.ImageDeskew;
import com.czairline.net.sourceforge.tess4j.TessAPI1;
import com.czairline.net.sourceforge.tess4j.Tesseract;
import com.czairline.net.sourceforge.tess4j.Tesseract1;
import com.czairline.net.sourceforge.tess4j.TesseractException;
import com.czairline.net.sourceforge.vietocr.ImageHelper;
import com.czairline.net.sourceforge.vietocr.ImageIOHelper;
import com.sun.jna.Pointer;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class Tesseract1Test {

    static final double MINIMUM_DESKEW_THRESHOLD = 0.05d;
    Tesseract1 instance;

    public Tesseract1Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new Tesseract1();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of doOCR method, of class Tesseract1.
     */
    @Test
    public void testDoOCR_File() throws Exception {
        System.out.println("doOCR on a PNG image");
        File imageFile = new File("eurotext.png");
        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String result = instance.doOCR(imageFile);
        System.out.println(result);
        assertEquals(expResult, result.substring(0, expResult.length()));
    }

    /**
     * Test of doOCR method, of class Tesseract1.
     */
    @Test
    public void testDoOCR_File_Rectangle() throws Exception {
        System.out.println("doOCR on a BMP image with bounding rectangle");
        File imageFile = new File("eurotext.bmp");
        Rectangle rect = new Rectangle(0, 0, 1024, 800); // define an equal or smaller region of interest on the image
        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String result = instance.doOCR(imageFile, rect);
        System.out.println(result);
        assertEquals(expResult, result.substring(0, expResult.length()));
    }

    /**
     * Test of doOCR method, of class Tesseract1.
     */
    @Test
    public void testDoOCR_List_Rectangle() throws Exception {
        System.out.println("doOCR on a PDF document");
        File imageFile = new File("eurotext.pdf");
        List<IIOImage> imageList = ImageIOHelper.getIIOImageList(imageFile);
        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String result = instance.doOCR(imageList, null);
        System.out.println(result);
        assertEquals(expResult, result.substring(0, expResult.length()));
    }

    /**
     * Test of doOCR method, of class Tesseract1.
     */
    @Test
    public void testDoOCR_BufferedImage() throws Exception {
        System.out.println("doOCR on a buffered image of a GIF");
        File imageFile = new File("eurotext.gif");
        BufferedImage bi = ImageIO.read(imageFile);
        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String result = instance.doOCR(bi);
        System.out.println(result);
        assertEquals(expResult, result.substring(0, expResult.length()));
    }

    /**
     * Test of deskew algorithm.
     */
    @Test
    public void testDoOCR_SkewedImage() throws Exception {
        System.out.println("doOCR on a skewed PNG image");
        File imageFile = new File("eurotext_deskew.png");
        BufferedImage bi = ImageIO.read(imageFile);
        ImageDeskew id = new ImageDeskew(bi);
        double imageSkewAngle = id.getSkewAngle(); // determine skew angle
        if ((imageSkewAngle > MINIMUM_DESKEW_THRESHOLD || imageSkewAngle < -(MINIMUM_DESKEW_THRESHOLD))) {
            bi = ImageHelper.rotateImage(bi, -imageSkewAngle); // deskew image
        }

        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String result = instance.doOCR(bi);
        System.out.println(result);
        assertEquals(expResult, result.substring(0, expResult.length()));
    }

    /**
     * Test of extending Tesseract1.
     */
    @Test
    public void testExtendingTesseract1() throws Exception {
        System.out.println("Extends Tesseract1");
        File imageFile = new File("eurotext.tif");

        String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
        String[] expResults = expResult.split("\\s");
        
        Tess1Extension instance1 = new Tess1Extension();
        List<Word> result = instance1.getWords(imageFile);
        
        //print the complete result
        for (Word word : result) {
            System.out.println(word);
        }
        
        List<String> text = new ArrayList<String>();
        for (Word word : result.subList(0, expResults.length)) {
            text.add(word.getText());
        }
        
        assertArrayEquals(expResults, text.toArray());
    }

    class Tess1Extension extends Tesseract1 {

        public List<Word> getWords(File file) {
            this.init();
            this.setTessVariables();

            List<Word> words = new ArrayList<Word>();
            try {
                BufferedImage bi = ImageIO.read(file);
                setImage(bi, null);

                TessAPI1.TessBaseAPIRecognize(this.getHandle(), null);
                TessAPI1.TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(this.getHandle());
                TessAPI1.TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
                TessAPI1.TessPageIteratorBegin(pi);

                do {
                    Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, TessAPI1.TessPageIteratorLevel.RIL_WORD);
                    String text = ptr.getString(0);
                    TessAPI1.TessDeleteText(ptr);
                    float confidence = TessAPI1.TessResultIteratorConfidence(ri, TessAPI1.TessPageIteratorLevel.RIL_WORD);
                    IntBuffer leftB = IntBuffer.allocate(1);
                    IntBuffer topB = IntBuffer.allocate(1);
                    IntBuffer rightB = IntBuffer.allocate(1);
                    IntBuffer bottomB = IntBuffer.allocate(1);
                    TessAPI1.TessPageIteratorBoundingBox(pi, TessAPI1.TessPageIteratorLevel.RIL_WORD, leftB, topB, rightB, bottomB);
                    int left = leftB.get();
                    int top = topB.get();
                    int right = rightB.get();
                    int bottom = bottomB.get();
                    Word word = new Word(text, confidence, new Rectangle(left, top, right - left, bottom - top));
                    words.add(word);
                } while (TessAPI1.TessPageIteratorNext(pi, TessAPI1.TessPageIteratorLevel.RIL_WORD) == TessAPI1.TRUE);

                return words;
            } catch (Exception e) {
                return words;
            } finally {
                this.dispose();
            }
        }
    }

    class Word {

        private String text;
        private float confidence;
        private Rectangle rect;

        public Word(String text, float confidence, Rectangle rect) {
            this.text = text;
            this.confidence = confidence;
            this.rect = rect;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * @return the confidence
         */
        public float getConfidence() {
            return confidence;
        }

        /**
         * @return the bounding box
         */
        public Rectangle getRect() {
            return rect;
        }
        
        @Override
        public String toString() {
            return String.format("%s\t[Confidence: %f Bounding box: %d %d %d %d]", text, confidence, rect.x, rect.y, rect.width, rect.height);
        }        
    }



    //根据实际验证码的色彩来判断哪里要变成白色
    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 300) {  //根据实际情况修改这里的300
            return 1;
        }
        return 0;
    }

    //根据实际验证码的色彩来判断哪里要变成黑色
    public static int isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300) {  //根据实际情况修改这里的300
            return 1;
        }
        return 0;
    }

    //扫描验证码所有的像素颜色过滤掉不要的颜色
    public static BufferedImage removeBackgroud4Tone(String picFile)
            throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isWhite(img.getRGB(x, y)) == 1) {
                    img.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }
        return img;
    }

    //将过滤都的黑白图片保存
    public static void handleImg(String file) {
        BufferedImage img;
        try {
            img = removeBackgroud4Tone(file);

            ImageIO.write(img, "JPG", new File("C:\\Users\\mulder\\JavaProjects\\czairline\\czauthImg.jpg"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public static String identifyCode(String fileName) {
        handleImg(fileName);
        Tesseract1 instance = new Tesseract1();
        File imageFile = new File(fileName); // instance.setLanguage("chi_sim");
        String result = null;
        try {
            result = instance.doOCR(imageFile);
        } catch (TesseractException e) { // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }



    public static  void main(String[] args) throws Exception {
/*
        Tesseract1Test t = new Tesseract1Test();
        t.testExtendingTesseract1();*/

        try {
            File imageFile = new File("czauthImg.jpg");
            Tesseract instance = Tesseract.getInstance(); // JNA Interface Mapping
            //Tesseract1 instance = new Tesseract1();
            String result = instance.doOCR(imageFile);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

      /*  Tesseract1Test t = new Tesseract1Test();
        t.handleImg("authImg.jpg");*/


        String txt = "<table width=\"750px;\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<th width=\"10%\" rowspan=\"2\">\n" +
                "\t\t\t<div class=\"flyimg\" title=\"飞行任务\"\n" +
                "\t\t\t\tonclick=\"showFlyOther(2,'6716','PEK','SYX','2016-02-17 19:10','27954325')\"></div>\n" +
                "\t\t\t</th>\n" +
                "\t\t\t<th width=\"9%\">航班号</th>\n" +
                "\t\t\t<th width=\"10%\">出发站</th>\n" +
                "\t\t\t<th width=\"9%\">抵达站</th>\n" +
                "\t\t\t<th width=\"16%\">起飞时间</th>\n" +
                "\t\t\t<th width=\"16%\">抵达时间</th>\n" +
                "\t\t\t<th width=\"9%\">机型</th>\n" +
                "\t\t\t<th width=\"9%\">飞机号</th>\n" +
                "\t\t\t<th width=\"16%\">机上岗位</th>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td class=\"flySize_11\">6716</td>\n" +
                "\t\t\t<td class=\"flySize_11\">PEK</td>\n" +
                "\t\t\t<td class=\"flySize_11\">SYX</td>\n" +
                "\t\t\t<td class=\"flySize_11\">2016-02-17 19:10</td>\n" +
                "\t\t\t<td class=\"flySize_11\">2016-02-17 23:15</td>\n" +
                "\t\t\t<td class=\"flySize_11\">333</td>\n" +
                "\t\t\t<td class=\"flySize_11\">B6098</td>\n" +
                "\t\t\t<td class=\"flySize_11\">FAT</td>\n" +
                "\t\t\t<td align=\"left\"></td>\n" +
                "\t\t</tr>\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t</table>\n" +
                "\t<div class=\"fly_other\" id=\"div_other2\"\n" +
                "\t\tstyle=\"width: 760px;\">\n" +
                "\t<div class=\"tit\">机组人员：</div>\n" +
                "\t<div class=\"list\" id=\"crewInfoDiv2\"></div>\n" +
                "\t</div>\n" +
                "\t</div>\n" +
                "\n" +
                "\t<div class=\"con_fly\">\n" +
                "\t<table width=\"750px;\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<th width=\"10%\" rowspan=\"2\">\n" +
                "\t\t\t<div class=\"flyimg\" title=\"飞行任务\"\n" +
                "\t\t\t\tonclick=\"showFlyOther(3,'6711','SYX','PEK','2016-02-18 09:00','27954325')\"></div>\n" +
                "\t\t\t</th>\n" +
                "\t\t\t<th width=\"9%\">航班号</th>\n" +
                "\t\t\t<th width=\"10%\">出发站</th>\n" +
                "\t\t\t<th width=\"9%\">抵达站</th>\n" +
                "\t\t\t<th width=\"16%\">起飞时间</th>\n" +
                "\t\t\t<th width=\"16%\">抵达时间</th>\n" +
                "\t\t\t<th width=\"9%\">机型</th>\n" +
                "\t\t\t<th width=\"9%\">飞机号</th>\n" +
                "\t\t\t<th width=\"16%\">机上岗位</th>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td class=\"flySize_11\">6711</td>\n" +
                "\t\t\t<td class=\"flySize_11\">SYX</td>\n" +
                "\t\t\t<td class=\"flySize_11\">PEK</td>\n" +
                "\t\t\t<td class=\"flySize_11\">2016-02-18 09:00</td>\n" +
                "\t\t\t<td class=\"flySize_11\">2016-02-18 12:45</td>\n" +
                "\t\t\t<td class=\"flySize_11\">333</td>\n" +
                "\t\t\t<td class=\"flySize_11\">B6098</td>\n" +
                "\t\t\t<td class=\"flySize_11\">FAT</td>\n" +
                "\t\t\t<td align=\"left\"></td>\n" +
                "\t\t</tr>\n" +
                "\t\t";

        ArrayList jizurenyunlist = new ArrayList();
      /*  while (true){
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

        for(int i=0;i<jizurenyunlist.size();i++){
            String jizhu = jizurenyunlist.get(i).toString();
            String[] jizutmp = jizhu.split(",");
            String actStrDtTmGmt = jizutmp[4];
            String fltNum = jizutmp[1];
            String portA = jizutmp[2];
            String portB = jizutmp[3];
            String seriesNum = jizutmp[5];

           // System.out.println(actStrDtTmGmt);
            System.out.println(seriesNum);


            "/newportal/cabin/scheduleplan-SchedulePlan-showCrewInfo.do",
        }


    }*/
    }

}