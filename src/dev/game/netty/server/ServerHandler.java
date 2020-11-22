package dev.game.netty.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import dev.game.netty.Util.JsonParser;
import dev.game.netty.database.DatabaseConnection;
import dev.game.netty.database.table.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private DatabaseConnection db = DatabaseConnection.getConnector();
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded of [SERVER]");
        Channel incoming = ctx.channel();
        for (Channel channel : channelGroup) {
            //사용자가 추가되었을 때 기존 사용자에게 알림
            channel.write("[SERVER] - " + incoming.remoteAddress() + "has joined!\n");
        }
        channelGroup.add(incoming);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 사용자가 접속했을 때 서버에 표시.
        System.out.println("User Access!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved of [SERVER]");
        Channel incoming = ctx.channel();
        for (Channel channel : channelGroup) {
            //사용자가 나갔을 때 기존 사용자에게 알림
            channel.write("[SERVER] - " + incoming.remoteAddress() + "has left!\n");
        }
        channelGroup.remove(incoming);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Read");
        String message = null;
        message = (String)msg;
        JSONObject json = JsonParser.createJson(message);

        String header = json.get("Header").toString();

        if(header.equals("Auth")) {
            authHandler(ctx, (JSONObject)json.get("body"));
        }
//        }else if(header.equals("InGame")){
//            //inGameHandler(ctx, json.get("body"));
//        }else if("Event"){
//            //eventHandler(ctx, json.get("body"));
//        }
    }

    private void authHandler(ChannelHandlerContext ctx, JSONObject body) {
        String function = body.get("Function").toString();
        JSONObject result = new JSONObject();
        JSONObject resultBody = new JSONObject();
        result.put("Header", "Auth");
        resultBody.put("Function", function);

        // LOGIN 1
        if(function.equals("1")) { // 파싱 데이터의 "Header"가 "login"일 떄
            User user = new User(); // 유저 객체 생성
            user.setId((String) body.get("id"));
            user.setPw((String) body.get("pw"));

            if (db.userLogin(user)) {
                resultBody.put("result", "SUCCESS");
                System.out.println("[SUCCESS] 로그인");
            } else {
                resultBody.put("result", "FAIL");
                System.err.println("[FAIL] 로그인");
            }
        }
        // SIGN UP 2
        else if(function.equals("2")) {
            User user = new User();
            user.setName((String)body.get("name"));
            user.setBirth((String)body.get("birth"));
            user.setPhone((String)body.get("phone"));
            user.setId((String)body.get("id"));
            user.setPw((String)body.get("pw"));

            if (db.createUser(user)) {
                resultBody.put("result", "SUCCESS");
                System.out.println("[SUCCESS] 회원가입");
            } else {
                resultBody.put("result", "FAIL");
                System.err.println("[FAIL] 회원가입");
            }
        }
        // ID FIND 3
        else if(function.equals("3")) {
            User user = new User();
            user.setName((String)body.get("name"));
            user.setBirth((String)body.get("birth"));

            String id = db.findID(user);
            if ( id != null) {
                resultBody.put("result", id);
                System.out.println("[SUCCESS] ID 찾기 : " + id);
            } else {
                resultBody.put("result", "FAIL");
                System.err.println("[FAIL] ID 찾기");
            }
        }
        // PW FIND 4
        else if(function.equals("4")) {
            User user = new User();
            user.setId((String)body.get("id"));
            user.setName((String)body.get("name"));
            String pw = db.findPW(user);
            if (pw != null) {
                resultBody.put("result", pw);
                System.out.println("[SUCCESS] PW 찾기 : " + pw);
            } else {
                resultBody.put("result", "FAIL");
                System.err.println("[FAIL] PW 찾기");
            }
        }else{
            resultBody.put("result", "FAIL");
            System.out.println("[WRONG] 잘못된 데이터");
        }
        result.put("body", resultBody.toJSONString());
        ctx.writeAndFlush(result + "\n");
    }


//    private void inGameHandler(ChannelHandlerContext ctx, JSONObject body) {
//
//    }
//
//    private void eventHandler(ChannelHandlerContext ctx, JSONObject body) {
//
//    }

}
