package net.taunty.asuka.discord;


import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;


public class DiscordPresence {
    private static final long CLIENT_ID = 1537337986788884530L;
    private static IPCClient client;
    private static boolean initialized = false;

    public static void init(){
        if(initialized){
            return;
        }

        client = new IPCClient(CLIENT_ID);
        client.setListener(new IPCListener(){
            @Override
            public void onReady(IPCClient client){
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setState("Asuka Client")
                        .setDetails("taunty the goat")
                        .setStartTimestamp(System.currentTimeMillis() / 1000L)
                        .setLargeImage("wfuxbwye", "Asuka Client")
                                .setActivityType(ActivityType.Playing);
                client.sendRichPresence(builder.build());

            }

            @Override
            public void onClose(IPCClient client, JsonObject json) {

            }

            @Override
            public void onDisconnect(IPCClient client, Throwable t) {

            }

            @Override
            public void onPacketSent(IPCClient client, Packet packet){
            }
            @Override
            public void onPacketReceived(IPCClient client, Packet packet){
            }

            @Override
            public void onActivityJoin(IPCClient client, String secret) {

            }

            @Override
            public void onActivitySpectate(IPCClient client, String secret) {

            }

            @Override
            public void onActivityJoinRequest(IPCClient client, String secret, User user) {

            }

        });

        try{
            client.connect();
            initialized=true;
        } catch(NoDiscordClientException | IllegalStateException e){
            e.printStackTrace();
        }
    }

    public static void shutdown(){
        if (!initialized || client == null){
            return;
        }
        client.close();
        initialized=false;
    }
}
