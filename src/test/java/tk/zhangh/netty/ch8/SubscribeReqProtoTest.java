package tk.zhangh.netty.ch8;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * Protobuf编码解码测试
 * Created by ZhangHao on 17/7/18.
 */
public class SubscribeReqProtoTest {
    private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
        return req.toByteArray();
    }

    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.setSubReqID(1);
        builder.setUserName("kangfoo");
        builder.setProductName("Netty 权威指南");
        List<String> adds = new ArrayList<>();
        adds.add("cd");
        adds.add("sh");
        adds.add("nb");
        builder.addAllAddress(adds);
        return builder.build();
    }

    public static void main(String args[]) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode : " + req.toString());
        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
        System.out.println("after encode : " + req2.toString());
        System.out.println("Assert equal : --> " + req2.equals(req));
    }
}
