package dev.game.netty.repository;

import org.json.simple.JSONObject;

public class Crewmate {
    //처음 한번만 전송
    String owner; // 캐릭터 주인 아이디
    String name; // 캐릭터 이름
    String color; // 캐릭터 컬러 변경시 업데이트

    //지속적으로 업데이트
    int x; // 캐릭터 x 좌표 Update
    int y; // 캐릭터 y 좌표 Update

    float drmX;
    float drmY;

    int maxHP; // 최대 체력
    int HP; // 현재 체력

    int frameNum;

    public Crewmate(JSONObject crewmate){
        this.owner = crewmate.get("owner").toString();
        this.name = crewmate.get("name").toString();
        this.color = crewmate.get("color").toString();

        this.x = (int)Double.parseDouble(crewmate.get("x").toString());
        this.y = (int)Double.parseDouble(crewmate.get("y").toString());

        this.drmX = Float.parseFloat(crewmate.get("drmX").toString());
        this.drmY = Float.parseFloat(crewmate.get("drmY").toString());

        this.maxHP = (int)Double.parseDouble(crewmate.get("maxHP").toString());
        this.HP = (int)Double.parseDouble(crewmate.get("HP").toString());

        this.frameNum = Integer.parseInt(crewmate.get("frameNum").toString());

    }

    public String getOwner() {
        return owner;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getInitCrewmateJson(){ // 처음 유저가 입장 할 때는 전부 받아야 하니까 전부 출력
        JSONObject result = new JSONObject();

        result.put("owner", owner);
        result.put("name", name);
        result.put("color", color);

        result.put("x", x);
        result.put("y", y);
        result.put("drmX", drmX);
        result.put("drmY", drmY);

        result.put("maxHP", maxHP);
        result.put("HP", HP);

        result.put("frameNum", frameNum);
        return result;
    }
    @SuppressWarnings("unchecked")
    public JSONObject getUpdateCrewmateJson(){ // 지속적으로 업데이트 해야할 정보
        JSONObject result = new JSONObject();

        result.put("owner", owner);

        result.put("x", x);
        result.put("y", y);
        result.put("drmX", drmX);
        result.put("drmY", drmY);

        result.put("HP", HP);

        result.put("frameNum", frameNum);
        return result;
    }

    public void update(JSONObject requestJson) { // 받은 Json 객체로 업데이트

        this.x = (int)Double.parseDouble(requestJson.get("x").toString());
        this.y = (int)Double.parseDouble(requestJson.get("y").toString());

        this.drmX = Float.parseFloat(requestJson.get("drmX").toString());
        this.drmY = Float.parseFloat(requestJson.get("drmY").toString());

        this.HP = (int)Double.parseDouble(requestJson.get("HP").toString());

        this.frameNum = Integer.parseInt(requestJson.get("frameNum").toString());
    }
}
