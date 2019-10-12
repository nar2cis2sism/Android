package engine.android.library.pay;

public class Pay {

    public static class IN {

        /** 微信或QQ返回的支付交易会话ID */
        public String prepayId;
        /** 随机字符串 */
        public String nonceStr;
        /** 时间戳 */
        public String timeStamp;
        /** 签名 */
        public String sign;
    }
}

