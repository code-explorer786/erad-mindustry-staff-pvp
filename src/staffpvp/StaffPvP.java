package staffpvp;

import arc.*;
import arc.util.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.game.*;

import staffpvp.helper.*;
import java.lang.*;

public class StaffPvP extends Plugin{

    public Session session = new Session();
    public UI ui = new UI();

    @Override
    public void init(){
        Events.on(Session.Begin.class, e -> {
            Log.info("mock begin");
        });

        Events.on(Session.End.class, e -> {
            Log.info("mock end");
        });

        Events.on(PlayerJoin.class, e -> {
            ui.playerJoin(e.player);
        });

        Events.on(PlayerLeave.class, e -> {
            ui.playerLeave(e.player);
        });

        Events.run(Trigger.update, () -> {
        });

        Vars.netServer.admins.addChatFilter((player, message) -> {
            if(player.admin
                || session.faction1.contains(player)
                || session.faction2.contains(player)) {
                return message;
           }
            return null;
        });

        var prevAssigner = Vars.netServer.assigner;
        Vars.netServer.assigner = (player, players) -> {
            if(session.ongoing){
                return Team.derelict;
            }
            return prevAssigner.assign(player, players);
        };
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.register("challenge", "", "Challenge griefers (staff only)",
            new CommandHandler.CommandRunner<Player>() {
                @Override
                public void accept(String[] args, Player player){
                    if(!player.admin){
                        player.sendMessage("You are [scarlet]NOT[] a staff!");
                        return;
                    }
                    if(session.ongoing){
                        player.sendMessage("There's a PvP ongoing. Try /end.");
                        return;
                    }
                    ui.uis.get(player, () -> ui.new PlayerUI(player))
                        .request(UI.selectionScreen(new Seq<Player>()));
                }
            }
        );
        handler.register("test", "", "Test screen",
            new CommandHandler.CommandRunner<Player>() {
                @Override
                public void accept(String[] args, Player player){
                    ui.uis.get(player, () -> ui.new PlayerUI(player))
                        .request(UI.testScreen());
                }
            }
        );
    };
}
