package staffpvp;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

public class Session {
    /** Is there a PVP ongoing? */
    public boolean ongoing = false;
    
    // TODO: May generalize this to any number of factions in the future?
    /** griefers */
    public Seq<Player> faction1 = new Seq<Player>();
    /** staff */
    public Seq<Player> faction2 = new Seq<Player>();

    public StartEndResult start(Seq<Player> player){
        if(ongoing){
            return StartEndResult.ongoing;
        }

        ongoing = true;
        Events.fire(new Begin());
        return StartEndResult.success;
    }

    public StartEndResult end(End.Verdict verdict){
        if(!ongoing){
            return StartEndResult.ongoing;
        }
        ongoing = false;

        End event = new End();
        event.faction1 = this.faction1;
        event.faction2 = this.faction2;
        event.verdict = verdict;

        faction1 = new Seq<Player>();
        faction2 = new Seq<Player>();

        Events.fire(event);
        return StartEndResult.success;
    }

    public static enum StartEndResult {
        success, ongoing;

        public void debug(){
            switch (this) {
                case success: {
                      Log.info("PvP start/end successful");
                }
                case ongoing: {
                      Log.err("start/end: it's already ongoing or it has already stopped");
                }
            }
        }
    }

    public static class Begin {}
    public static class End {
        public Seq<Player> faction1;
        public Seq<Player> faction2;
        public Verdict verdict;

        public static enum Verdict { 
            faction1win, faction2win,
            faction1left, faction2left,
            draw, quit
        }
    }
}
