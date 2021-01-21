package elm.data.test.myservlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.lang.Long;

/**
 * @version 1.0
 * @author: 侠怀
 * @create: {YEAR}-YEAR−{MONTH}-{DAY}DAY{TIME}
 * @className: PACKAGENAME.{NAME}
 * @description: TODO
 */
public class CustomerData extends HttpServlet {
    private String message;
    private  String name = this.getClass().getName();
    private  Logger logger = Logger.getLogger(name);
    protected   StringBuffer SHA256Sign;
    private static Long shopid = new Long(810863478) ;
    private static String appid="29893dc776c7dcd13c3b17ad93cdfa9d";
    private static String appsecret="44ed2fb9cabf113768fb1213ab60973a";
    protected StringBuffer TokenURL ;
    protected String Token;
    protected StringBuffer APISign;
    protected String OrderDetailReturn;

    public void init() {
        message = "Hello my friend!!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        signForToken();
        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("<h1>sign:" + SHA256Sign + "</h1>");
        out.println("</body></html>");

    }
    public void destroy() {
    }

    public void getShopToken(){
        //本地生成签名
        signForToken();
        //获取TOKEN
        String response = doHttpGet(String.valueOf(TokenURL),"GBK");
        JSONObject resultjson = (JSONObject) JSONObject.parseObject(String.valueOf(response));
        String code = resultjson.get("code").toString();
        if (code.equals("0"))  {
            JSONObject tokenjson = resultjson.getJSONObject("result");
            Token = tokenjson.get("token").toString();
            System.out.println("TOKEN:"+Token);
        }
    }

    public void getShopOrderList(){
        Long currentTime = System.currentTimeMillis();
        if(Token == null || Token.equals("") ){
            System.out.println("Token 没有获取");
            signForToken();
        }else{
            /*if(APISign == null || APISign.length()<=0){
                //获取接口sign
                signForAPI(currentTime);
            }*/
            //获取接口sign
            signForAPI(currentTime);
            JSONObject json = new JSONObject(true);
            /*json.put("appKey",appid );
            json.put("shopIdenty",shopid);
            System.out.println("shopid:" + shopid);
            json.put("version","1.0");
            json.put("timestamp",currentTime);
            json.put("sign",APISign.toString());*/
            long timestamp = new Long(0);
            long timestamp2 = new Long(0);
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date date = df.parse("2021-01-13");
                java.util.Date date2 = df.parse("2021-01-14");
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                 timestamp = cal.getTimeInMillis();
                cal.setTime(date2);
                 timestamp2 = cal.getTimeInMillis();
            } catch (ParseException e){ }

            json.put("shopIdenty",shopid);
            json.put("startTime",timestamp);
            json.put("endTime",timestamp2);
            json.put("timeType",2);
            System.out.println(json.toString());
            StringBuffer orderURL = new StringBuffer();
            //orderURL.append("https://openapi.keruyun.com/open/v1/data/order/export2");
            orderURL.append("https://openapi.keruyun.com/open/v1/data/order/export2?appKey=").append(appid).append("&shopIdenty=").append(shopid.toString()).append("&version=1.0&timestamp=").append(currentTime.toString()).append("&sign=" ).append(APISign.toString());
            System.out.println(orderURL.toString());
            String OrderReturn = doHttpPost( orderURL.toString(), json);
            System.out.println("订单列表返回值"+OrderReturn);
            JSONObject resultOrderlistjson = (JSONObject) JSONObject.parseObject(String.valueOf(OrderReturn));

        }

    }

    public void getShopOrderDetail(){
        Long currentTime = System.currentTimeMillis();
        if(Token == null || Token.equals("") ){
            System.out.println("Token 没有获取");
            signForToken();
        }else{
            //获取signforapi
            signForAPI(currentTime);

            List<Long> orderList = new ArrayList<Long>();
            orderList.add(6755025594049625988L);
            //orderList.add(445644745524532224L);
            JSONObject json = new JSONObject(true);
            json.put("ids",orderList);
            json.put("shopIdenty",shopid);
            System.out.println(json.toString());
            StringBuffer orderURL = new StringBuffer();
            orderURL.append("https://openapi.keruyun.com/open/v1/data/order/exportDetail?appKey=").append(appid).append("&shopIdenty=").append(shopid.toString()).append("&version=1.0&timestamp=").append(currentTime.toString()).append("&sign=" ).append(APISign.toString());
            System.out.println(orderURL.toString());
            String OrderReturn = doHttpPost( orderURL.toString(), json);
            OrderDetailReturn = OrderReturn;
            System.out.println("订单列表返回值"+OrderReturn);

        }

    }
    //向服务器推送get
    public static String doHttpGet(String url, String charset) {
        //1.生成HttpClient对象并设置参数
        HttpClient httpClient = new HttpClient();
        //设置Http连接超时为5秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        //2.生成GetMethod对象并设置参数
        GetMethod getMethod = new GetMethod(url);
        //设置get请求超时为5秒
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
        //设置请求重试处理，用的是默认的重试处理：请求三次
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String response = "";
        //3.执行HTTP GET 请求
        try {
            int statusCode = httpClient.executeMethod(getMethod);
            //4.判断访问的状态码
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("请求出错：" + getMethod.getStatusLine());
            }
            //5.处理HTTP响应内容
            //HTTP响应头部信息，这里简单打印
            Header[] headers = getMethod.getResponseHeaders();
            for(Header h : headers) {
                System.out.println(h.getName() + "---------------" + h.getValue());
            }
            //读取HTTP响应内容，这里简单打印网页内容
            //读取为字节数组
            byte[] responseBody = getMethod.getResponseBody();
            response = new String(responseBody, charset);
            System.out.println("-----------response:" + response);
            //读取为InputStream，在网页内容数据量大时候推荐使用
            //InputStream response = getMethod.getResponseBodyAsStream();
        } catch (HttpException e) {
            //发生致命的异常，可能是协议不对或者返回的内容有问题
            System.out.println("请检查输入的URL!");
            e.printStackTrace();
        } catch (IOException e) {
            //发生网络异常
            System.out.println("发生网络异常!");
        } finally {
            //6.释放连接
            getMethod.releaseConnection();
        }
        return response;
    }

    // 向服务器推送post
    public static String doHttpPost(String url, JSONObject json){
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);

        postMethod.addRequestHeader("accept", "*/*");
        postMethod.addRequestHeader("connection", "Keep-Alive");
        //设置json格式传送
        postMethod.addRequestHeader("Content-Type", "application/json;charset=GBK");
        //必须设置下面这个Header
        postMethod.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        //添加请求参数
        //postMethod.addParameter("commentId", json.getString("commentId"));
        System.out.println("开始写参数");
        /*LinkedHashMap<String, String> jsonMap = JSON.parseObject(json.toString(), new TypeReference<LinkedHashMap<String, String>>() {
        });
        for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
            postMethod.addParameter(entry.getKey().toString(), entry.getValue().toString());
        }*/
        String transJson = json.toString();
        try {
            RequestEntity se = new StringRequestEntity(transJson, "application/json", "UTF-8");
            postMethod.setRequestEntity(se);
        }catch (IllegalArgumentException e) {
            System.out.println("非法的URL：" + url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String res = "";
        try {
            int code = httpClient.executeMethod(postMethod);
            if (code == 200){
                res = postMethod.getResponseBodyAsString();
                System.out.println("订单接口返回值："+res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @Description: 获取token时签名验证（只在获取token时调用一次）,本地执行
     * @param
     * @throws
     */
    public void signForToken() {
        Map<String, Object> params = new TreeMap<>();
        Long currentTime = System.currentTimeMillis();
        params.put("appKey", appid);
        params.put("shopIdenty", shopid);
        params.put("version", "1.0");
        params.put("timestamp",currentTime.toString() );
        StringBuilder sortedParams = new StringBuilder();
        params.entrySet().stream().forEachOrdered(paramEntry ->
                sortedParams.append(paramEntry.getKey()).append(paramEntry.getValue()));
        sortedParams.append(appsecret);
        SHA256Sign = new StringBuffer();
        TokenURL = new StringBuffer();
        try {
            SHA256Sign = getSign(sortedParams.toString());
            TokenURL=TokenURL.append("https://openapi.keruyun.com/open/v1/token/get?appKey=").append(appid).append("&shopIdenty=").append(shopid).append("&version=1.0&timestamp=").append(currentTime.toString()).append("&sign=" ).append(SHA256Sign.toString());
            System.out.println(TokenURL);
        } catch (NoSuchAlgorithmException e) {
            logger.info("获取签名出错" + e.getMessage());
        }
        /* if (!StringUtils.equals(sign, SHA256Sign)) {// 签名校验
            String msg = String.format("sign=%s", sign);
            System.out.println("签名校验不通过:" + msg);
        }*/
    }

    /**
     * 普通接口加密，获取到token之后
     **/
    public  void signForAPI(Long currentTime) {
        Map<String, Object> params = new TreeMap<>();
        //Long currentTime = System.currentTimeMillis();
        params.put("appKey", appid);
        params.put("shopIdenty", shopid);
        params.put("version", "1.0");
        params.put("timestamp", currentTime.toString());
        StringBuilder sortedParams = new StringBuilder();
        params.entrySet().stream().forEachOrdered(paramEntry ->
                sortedParams.append(paramEntry.getKey()).append(paramEntry.getValue()));
        sortedParams.append(Token);//请替换成真实的token
        System.out.println(sortedParams);
        try {
            APISign = getSign(sortedParams.toString());
            System.out.println(APISign + "       " + APISign.length());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: SHA256加密字符串
     * @param
     * @return String
     * @throws NoSuchAlgorithmException
     */
    private static StringBuffer getSign(String sortedParams) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(sortedParams.getBytes());
        byte byteBuffer[] = messageDigest.digest();
        StringBuffer strHexString = new StringBuffer();
        for (int i = 0; i < byteBuffer.length; i++){
            String hex = Integer.toHexString(0xff & byteBuffer[i]);
            if (hex.length() == 1) {
                strHexString.append('0');
            }
            strHexString.append(hex);
        }
        // 得到返回結果
        //String returnsign = strHexString.toString();
        return strHexString;
    }
}
