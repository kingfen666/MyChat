package org.kingfen.ChatGpt;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kingfen.ChatGpt.ResponseJson.Entity;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class MakeChat {
    static ArrayList<Dialogue> list=new ArrayList<>();//聊天记录存储
    private MakeChat(){}
    private static final String url = "https://api.chatanywhere.com.cn/v1/chat/completions";//访问地址
    public static void chat(String message){
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            list.add(new Dialogue("user",message));//添加记录
            // 设置请求头信息
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer sk-5UH81xbNL5s3y9K4a82g6TGWkAO5gMofuAt2A9SJTFFo53Nd");
            // 设置请求体
            StringEntity entity = new StringEntity("{\"model\":\"gpt-3.5-turbo\",\"messages\":"+JSON.toJSONString(list)/*生成记录*/+",\"temperature\":0.7}", StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            // 发送请求
            CloseableHttpResponse response = httpClient.execute(httpPost);
            // 处理响应
            HttpEntity entity1 = response.getEntity();
            String s = new String(entity1.getContent().readAllBytes(),StandardCharsets.UTF_8);
            Entity x = JSON.parseObject(s, Entity.class);
            httpClient.close();
            String content = x.getChoices().getMessage().getContent();
            System.out.println("ChatGpt:"+content);

            list.add(new Dialogue("assistant",content));//添加ai回答记录
            if (content.matches("(?=.*[a-zA-Z]).*")){
                textToSpeech(content,"EN");
            }else {
                textToSpeech(content, "ZH");
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
    public static void textToSpeech(String text,String language) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://v2.genshinvoice.top/run/predict");
            //设置请求参数
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Cookie","_gid=GA1.2.1383047020.1702119163; _gat_gtag_UA_156449732_1=1; _ga_R1FN4KJKJH=GS1.1.1702134962.2.1.1702135547.0.0.0; _ga=GA1.1.1749057500.1702119163");
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String session_hash=uuid.substring(0, 10);

            StringEntity stringEntity=new StringEntity(format("{\"data\":[\"{}\",\"神里绫华_ZH\",4,0.2,0.5,0.9,1.3,\"{}\",null],\"event_data\":null,\"fn_index\":2,\"session_hash\":\"{}\"}",text,language,session_hash), StandardCharsets.UTF_8);
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse execute = httpClient.execute(httpPost);
            HttpEntity entity = execute.getEntity();
            byte[] bytes = entity.getContent().readAllBytes();
            String x = new String(bytes);

            x=x.replace("{\"data\":[\"Success\",{\"name\":\"","");
            x=x.substring(0,x.indexOf(".wav"))+".wav";//获取文件地址
            play("https://v2.genshinvoice.top/file=" + x);
            System.out.print("我:");
        } catch (Exception e) {

        }
    }
    private static void play(String url){
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new URL(url));

            // 获取音频流的格式
            AudioFormat audioFormat = audioInputStream.getFormat();

            // 创建DataLine.Info对象，用于描述音频格式
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            // 根据音频格式创建SourceDataLine对象
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);

            // 打开SourceDataLine
            sourceDataLine.open(audioFormat);

            // 启动音频播放线程
            sourceDataLine.start();

            // 定义缓冲区
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            // 循环读取音频数据并播放
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            // 停止播放，关闭资源
            sourceDataLine.drain();
            sourceDataLine.close();
            audioInputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String format(String s,Object...objects){
        for (Object object : objects) {
            s=s.replaceFirst("\\{}",object.toString());
        }
        return s;
    }
}
