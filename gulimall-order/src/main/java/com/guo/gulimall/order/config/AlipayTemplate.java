package com.guo.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.guo.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.stereotype.Component;


@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000121626682";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDA8ZczNb/vF2SfHritN05KQRr+8MVtWjc8vTU6i0VDPk7aUJHyiY45jDNrEIceYHSQAq0nw9NKG1mkCMf13qNZoTt/RZoLNy+H3s8vweI1tn5AEmX6QLIECj2IU6FNXqx4mNul9NyxKGnjWAhypt8gxd3J3RnxHd6HLQfwv4FJODVK/WBZYbBEDdQk+lJs69DUAKIh09nAewhQhwIco35jbaEAai01fJL+aXiNoP/BwKoWnrzQUzlGFB882m/JCBjk87/U2cRd9fUQkhMNwNByR/2C4viEdDpqKxX4ZEtJhVCMlZTrs8WfEVj5NmJHcl/nhn/LGA9K6Cib5K0oJd0NAgMBAAECggEBAKyEmCRy7/4xlYftaUWASSF920yPLohbfT5zE7AYLUM16D2ugwExW5k7MhKOQbK4niDSM891emwtzlYsf9bpTnfrAY8IRfe3TWmYoPrbyo8qcPKLQOGljUatfV1zIaxTdbWTP/rA2cASGbPs2AjkCmh8Y5aGexasFXganJn4MfqrlzQ8ekYEN2/SdCD8oTAbt834VfF9bVL3ju0cATW6yRzOR0j1TgLC5un12t7CaiWEqgOaUoF1hNSXk180AKaP2Zy4nYjaQ/U4L8UZD8C8lZ+46pk7Tchup5LeYDqyIbtRcsdBoS9UkF1O0JR4SnyFvVML3c1nIZnW0nUASice7EECgYEA9NiWd4Tn5CD8yGkFcjAEL44bDr9yloMK89MrLutl1bHg/WGiXgl2MJz59DAYfM/+ylVksKZWwOd3eWLH730OLYTZuHCS93wxYHFVb9KakQNJsYZ80MWyH7D58ycvr7QgSz+Tj5JLS/PsefivxbkcJgGxcMLXm44rJFh6649CyJECgYEAybu20JBvlflWZPeC6fWYZ4ibGDS1S4D5QuLvYCYYrKhguG+ZwTxQ7I7blNLnu0dahZ+c2VgbMIABGNK/rRPd/n1/W/MiE/v+Dyu9Eq1FVW2yptzKx2Up3EMdmDCp8bJqOS86rkFpfXllqvPRYs/JjjSV5aalqUMsek0krHfwKr0CgYEA03+gAiARO5NvKCLn7Gojw3xqTQP9FJyfwlzfyYjXj482/UjwO3DK4lVG6LL/BBmbSuYWsy0MdVe8cWNtt6b7r0A53cOIgDWMZXWgypZOpAMGDk5Q1Ppg0lVKo8puu8TJvJZvfa5uUm6jD/1MwGwVL5N41iHIovtcsumnTIGUu7ECgYEAidyJk8CRMUmjG/717VMUYvS9IO6sUrwCHA6gDfIa/KtY5dIGnkLM/symDO+Qw7CAkT7B3tpSQnPHzCICMZKZLBixl7HxQPz943o9jOzyq+36oAPVZCyjTc5kBCSFjxIhe5DmJj23jLMQoqJbHo7nrBINulKHdcPdOntLq72PWHUCgYAEwGqRzzngBy2Kzf/NUZDn/SVnZQc1EopOpNdkkLIGgg4LBznIZ9LdQNwv8GMN6Sjo8LJ+V4yDjQg+IZFz3q6AqeI6CXQ/Y1goLWiA7eS1kGkLz+giWQ45p/pVNj+2fP/yVdCbuHGZkFPffDQdFmkeOAq9Jq44y5RxDtow/BYemA==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo37tjJe1z0nm/I57C8D7WKiUSP/uwa/F+qR8gbUR1+mE5X7XDhk2JHPMtgQoHw4sL9HcFC/hzaepszrykK4WQstrHsPpultCcu+LMyXDCrUg9vsHUr+C5KtMtwkMKzJps9+joDvlgCjJ9B//4EqCd+Fdg4B28OJW5yNQiNfa/1rzH7HBMkDhTHoEfMXyqXX6e+Hyai+LVfRYq19mnC7GGvxudVZSZuvF6Sg1uW5KjFHaRPPUpB/j+2mJxEfctjyYLMb12sxx4OZmZfyK39pkQVIPkDnY9vsQ1jXR4uAvOculXKfcr2XZXnndFoJf5Y0Lo8np7oSr7RJs/qn117iuZwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http:cheney.vip:8000/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
