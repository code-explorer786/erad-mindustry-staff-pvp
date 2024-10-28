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

    public static class Begin {
        public Seq<Player> faction1;
        public Seq<Player> faction2;
    }
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
