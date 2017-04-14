package LhtUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by lht on 2017/2/25.
 */

public class OkhttpTool {

    private static OkhttpTool okhttpTool;
    private static OkHttpClient okHttpClient;
    private static Request request;
    private static FormBody formBody;
    private Call call;


    public OkhttpTool() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(30*1000, TimeUnit.SECONDS)
                .writeTimeout(10*1000, TimeUnit.SECONDS)
                .readTimeout(10*1000, TimeUnit.SECONDS)
//                .cookieJar(new CookieJar() {
//                    @Override
//                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                        //保存cookie信息,密码，表单等信息
//                    }
//
//                    @Override
//                    public List<Cookie> loadForRequest(HttpUrl url) {
//                        //从保存位置读取，注意此处不能为空，否则会导致空指针
//
//                        return new ArrayList<Cookie>();
//                    }
//                })
                .build();

    }


    public static OkhttpTool getOkhttpTool() {

        if (null == okhttpTool) {

            synchronized (OkhttpTool.class) {
                if (null == okhttpTool) {
                    okhttpTool = new OkhttpTool();
                }
            }
        }

        return okhttpTool;
    }


    public void get(String url, Callback callback) {

        request = new Request.Builder()
                .url(url)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(callback);

    }


    public void post(String url, Map<String,Object> map, Callback callback) {

     FormBody.Builder formBody = new FormBody.Builder();

        if (null!=map) {
            for (Map.Entry<String, Object> entity : map.entrySet()) {
                try {
                    String o = URLEncoder.encode(String.valueOf(entity.getValue()), "GBK");
                    formBody.add(entity.getKey(), o);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        request = new Request.Builder()
                .url(url)
                .post(formBody.build())
                .build();

        okHttpClient.newCall(request).enqueue(callback);

    }


    //单张图片上传
    public void upLoadImage(String url, File f, Callback callback) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("imgFile", "imgFile.jpg", RequestBody.create(MediaType.parse("image/*"), f));

        request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        okHttpClient.newCall(request).enqueue(callback);

    }


}
