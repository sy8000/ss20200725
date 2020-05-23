package cn.besbing.Conctrollers;


import ch.qos.logback.core.net.SyslogOutputStream;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.ibatis.annotations.Param;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ImageProcessor {

    Logger logger = LoggerFactory.getLogger(ImageProcessor.class);

    @RequestMapping(value = "/processUploadImages",method = RequestMethod.POST)
    @ResponseBody
    public JSONObject batchImagesUpload(@PathVariable(value="file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject jsonObject = new JSONObject();
        InputStream inputStream = file.getInputStream();
        byte[] data = readInputStream(inputStream);
        //new一个文件对象用来保存图片，默认保存当前工程根目录
        File imageFile = new File("sheny.jpg");
        //创建输出流
        FileOutputStream outStream = new FileOutputStream(imageFile);
        //写入数据
        outStream.write(data);
        //关闭输出流
        outStream.close();

        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String fileName = formatter.format(date) + ".jpg" ;
        Thumbnails.of(imageFile).width(1420).height(1065).toFile(fileName);
        imageFile.delete();

        jsonObject.put("code",1);
        jsonObject.put("msg","上传成功");
        String arr = "<a href='/DownImages?fileName='"+ fileName +">"+ fileName + "</a>" ;
        jsonObject.put("data",arr);

        /*File f = new File("e:/sheny_trance.jpg");
        response.reset();
        response.setContentType("multipart/force-download");
        String name="ExportImage";
        name = new String(name.getBytes(), "ISO-8859-1");
        response.setHeader("Content-Disposition","attachment; filename=sheny_trance.jpg");
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(f);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            return "下载成功";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "下载失败";*/
        /*imageFile.delete();
        jsonObject.put("code",1);
        jsonObject.put("msg","上传成功");
        String arr[] = {"sheny_trance.jpg"};
        jsonObject.put("data",arr);*/

        /*File f = new File("sheny_trance.jpg");
        FileInputStream ips = new FileInputStream(f);
        response.setContentType("multipart/form-data");
        response.addHeader("Content-Disposition", "attachment; filename=sheny.jpg");
        ServletOutputStream out1 = null;
        out1 = response.getOutputStream();
        //读取文件流
        int len = 0;
        byte[] buffer = new byte[1024 * 10];
        while ((len = ips.read(buffer)) != -1){
            out1.write(buffer,0,len);
        }
        out1.flush();*/
        return jsonObject;
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    @RequestMapping(value = "/uploadImage",method = RequestMethod.GET)
    public String uploadImageUrl(){
        return "pages/uploadImages";
    }


}
