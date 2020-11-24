package dev.game.netty.server;

import dev.game.netty.Util.JsonParser;
import dev.game.netty.database.DatabaseConnection;
import dev.game.netty.database.table.User;
import dev.game.netty.repository.Crewmate;
import dev.game.netty.repository.Room;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private DatabaseConnection db = DatabaseConnection.getConnector();
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded of [SERVER]");
        Channel incoming = ctx.channel();
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
        channelGroup.remove(incoming);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String message = null;
        message = (String)msg;
        JSONObject json = JsonParser.createJson(message);

        if(json.get("Header") == null) {
            errorHandler(ctx, message);
            return;
        }

        String header = json.get("Header").toString();

        switch (header) {
            case "InGame":
                inGameHandler(ctx, (JSONObject) json.get("Body"));
                break;
            case "Auth":
                authHandler(ctx, (JSONObject) json.get("Body"));
                break;
            case "Event":
                eventHandler(ctx, (JSONObject) json.get("Body"));
                break;
        }
    }

    private void errorHandler(ChannelHandlerContext ctx, String msg){

    }

    private void inGameHandler(ChannelHandlerContext ctx, JSONObject body){
        String function = body.get("Function").toString();
        JSONObject result = new JSONObject();
        JSONObject resultBody = new JSONObject();
        result.put("Header", "InGame");
        resultBody.put("Function", function);

        if(function.equals("6")) {
            Room room = Room.getRoom( Integer.parseInt(body.get("code").toString()) );
            if(room != null) {
                room.update((JSONObject)body.get("crewmate"));
                resultBody.put("code", room.getRoomCode());

                JSONObject crewmatesJson = new JSONObject(); // 크루메이트들 담을 Json

                for (Crewmate crewmate : room.getCrewmates()){ // 기존 방에 있던 크루원들 초기 정보
                    crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson()); // 크루메이트 담음
                }
                crewmatesJson.put("crewmates_size", room.getCrewmates().size()); // 방인원 현재 사이즈
                resultBody.put("crewmates", crewmatesJson); // 크루메이트들 정보
            }
        }
        result.put("Body", resultBody);
        ctx.writeAndFlush(result + "\n");
    }

    private void eventHandler(ChannelHandlerContext ctx, JSONObject body) throws ParseException {
        String function = body.get("Function").toString();
        JSONObject result = new JSONObject();
        JSONObject resultBody = new JSONObject();
        result.put("Header", "Event");
        resultBody.put("Function", function);

        if(function.equals("5")) {
            Room room = Room.getRoom(); // 방 받아옴
            resultBody.put("code", room.getRoomCode());

            room.enter(ctx, JsonParser.createJson(body.get("crewmate").toString()));

            JSONObject crewmatesJson = new JSONObject();
            for (Crewmate crewmate : room.getCrewmates()){ // 기존 방에 있던 크루원들 초기 정보
                crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson()); // 제이슨으로 받아서 0, 1, 2, 3, 4로 번호 매겨서 제이슨 생성
            }
            resultBody.put("crewmates", crewmatesJson);

            result.put("Body", resultBody);
            ctx.writeAndFlush(result + "\n");

        }else if(function.equals("9")) {
            int code = Integer.parseInt(body.get("code").toString());
            String owner = body.get("owner").toString();
            Room room = Room.getRoom(code); // 방 받아옴

            if(room != null) {
                room.getCrewmates().removeIf(crewmate -> crewmate.getOwner().equals(owner));
                resultBody.put("owner", owner);
                result.put("Body", resultBody);
                room.getChannels().remove(ctx.channel());
                for(Channel channel : room.getChannels()){
                    System.out.println("EXIT : " + result);
                    channel.writeAndFlush(result + "\n");

                }
            }
        }

    }

    private void authHandler(ChannelHandlerContext ctx, JSONObject body) {
        String function = body.get("Function").toString();
        JSONObject result = new JSONObject();
        JSONObject resultBody = new JSONObject();
        result.put("Header", "Auth");
        resultBody.put("Function", function);

        // LOGIN 1
        if(function.equals("1")) {
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
        result.put("Body", resultBody.toJSONString());
        ctx.writeAndFlush(result + "\n");
    }
}
