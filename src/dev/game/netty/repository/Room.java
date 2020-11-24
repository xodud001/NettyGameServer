package dev.game.netty.repository;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Room { //게임 방

    private static ArrayList<Room> rooms = new ArrayList<>();

    int roomCode; //방 번호
    ArrayList<Crewmate> crewmates; // 참가자들

    ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ChannelGroup getChannels(){
        return channels;
    }
    public Room(int roomCode){
        this.roomCode = roomCode;
        this.crewmates = new ArrayList<>();
    }

    //방에 처음 입장한 유저 정보 초기화 하고 다른 유저들에게 알림
    public void enter(ChannelHandlerContext ctx, JSONObject body) {
        Crewmate crewmate = new Crewmate(body);
        crewmates.add(crewmate);
        channels.add(ctx.channel());
    }

    public int getRoomCode(){
        return roomCode;
    }

    public ArrayList<Crewmate> getCrewmates(){
        return crewmates;
    }

    public static Room getRoom(){
        if(rooms.size() == 0) // 방 하나도 없음
            rooms.add(new Room(0));

        for(Room room : rooms)
            if(room.crewmates.size() < 5) // 만들어진 방 중에 자리 있으면 그 방 리턴
                return room;

        if(rooms.get(rooms.size()-1).crewmates.size() >= 5){ // 방 다 꽉참
            int temp = rooms.get(rooms.size()-1).roomCode; // 끝방 코드 번호 가져옴
            rooms.add(new Room(temp + 1)); // 끝에 방 하나 새로 생성
        }

        return rooms.get(rooms.size()-1); // 끝방 리턴
    }

    public static Room getRoom(int code){
        for(Room room : rooms){
            if(room.roomCode == code)
                return room;
        }
        return null;
    }

    public void update(JSONObject requestJson) { // 방 정보 업데이트
        for(Crewmate crewmate : crewmates){
            if( crewmate.owner.equals(requestJson.get("owner")) ){
                crewmate.update(requestJson);
            }
        }
    }

}
