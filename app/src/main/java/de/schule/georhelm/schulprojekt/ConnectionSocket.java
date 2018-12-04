package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionSocket {
    private Socket socket;
    private String token;
    public static ConnectionSocket getSocket() {
        return ConnectionSocket.connectionSocket;
    }
    public static void setSocket(ConnectionSocket socket) {
        ConnectionSocket.connectionSocket = socket;
    }
    private static ConnectionSocket connectionSocket;

    public ConnectionSocket(String token) {
        this.token = token;
    }

    public boolean init(final Context context){
        try{
            IO.Options options = new IO.Options();
            options.query = "token=" + token;
            String[] transportArray = {"websocket"};
            options.transports = transportArray;
            socket = IO.socket("http://siffers.de:1234",options);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() { //Disconnect event

                @Override
                public void call(Object... args) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }

            });
            socket.connect();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public boolean logOut(){
        try{
            socket.disconnect();
            //socket.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public void playerInput(boolean lanceUp) {
        JSONObject data = new JSONObject();
        try {
            data.put("type", "lance");
            data.put("value", lanceUp);
            this.socket.emit("game_input", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void playerReady(){
        socket.emit("player_ready");
    }

    public void startSingleplayerGame(final MenuActivity menuActivity){
        startGame(menuActivity);
        socket.emit("start_singleplayer");
    }

    public void startMultiplayerGame(final MenuActivity menuActivity){
        startGame(menuActivity);
        socket.emit("start_multiplayer");
        socket.once("searching_multiplayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                menuActivity.showSearchingGame();
            }
        });
    }

    private void startGame(final MenuActivity menuActivity) {
        socket.once("found_game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                menuActivity.startGame((JSONObject)args[0]);
            }
        });
    }

    public void initGame(final GameView gameView){
        socket.off("game_update");
        socket.on("game_update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{
                    JSONObject jsonObject = (JSONObject)args[0];
                    String type = jsonObject.getString("type");
                    switch(type) {
                        case "countdown":
                            gameView.countDown(jsonObject.getInt("value"));
                            break;
                        case "partialUpdate":
                            JSONObject value = jsonObject.getJSONObject("value");
                            JSONObject player = value.getJSONObject("player1");
                            JSONObject enemy = value.getJSONObject("player2");

                            int playerPos = player.getInt("position");
                            int enemyPos = enemy.getInt("position");
                            int playerLanceAngle = player.getInt("weaponAngle");
                            int enemyLanceAngle = enemy.getInt("weaponAngle");
                            gameView.setPlayerPositions(playerPos,enemyPos);
                            gameView.setLanceAngles(playerLanceAngle, enemyLanceAngle);
                            break;
                        case "gameEnd":
                            finishRound(jsonObject,gameView,true);
                            break;
                        case "roundEnd":
                            finishRound(jsonObject,gameView,false);

                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    private void finishRound(JSONObject jsonObject, GameView gameView,boolean isLastRound){
        try{
            JSONObject values = jsonObject.getJSONObject("value");
            JSONObject playerJson = values.getJSONObject("player1");
            int playerSpeed = playerJson.getInt("speed");
            int playerHitpoints = playerJson.getInt("hitpoints");
            int playerWeaponHeight = playerJson.getInt("weaponHeight");
            int playerPointHit = playerJson.getInt("pointHit");

            JSONObject enemyJson = values.getJSONObject("player2");
            int enemySpeed = enemyJson.getInt("speed");
            int enemyHitpoints = enemyJson.getInt("hitpoints");
            int enemyWeaponHeight = enemyJson.getInt("weaponHeight");
            int enemyPointHit = enemyJson.getInt("pointHit");
            gameView.endRound(enemySpeed,enemyHitpoints,enemyWeaponHeight,enemyPointHit,playerSpeed,playerHitpoints,playerWeaponHeight,playerPointHit, isLastRound);
        }catch(Exception e){
        }
    }

    public void cancelSearch(){
        socket.emit("cancel_search");
    }

    public void getAvailableEquipment(final EquipmentActivity equipmentActivity){
        socket.emit("get_equipment");
        socket.once("recieve_equipment", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject equipment = (JSONObject)args[0];
                equipmentActivity.fillEquipment(equipment);
            }
        });
    }

    public void saveEquipment(int mountId){
        JSONObject equipment = new JSONObject();
        try {
            equipment.put("mountId",mountId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("set_equipment",equipment);
    }

    public void leaveGame() {
        socket.emit("leave_game");
    }
}