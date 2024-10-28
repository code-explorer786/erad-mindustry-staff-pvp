package staffpvp;

import arc.util.*;
import arc.func.*;
import arc.struct.*;
import mindustry.ui.*;
import mindustry.gen.*;

import staffpvp.helper.*;

import java.lang.*;

/** Stateful per-player user interface consisting of dialogs and commands. */
public class UI {
    public static enum Single { uniq }

    public ObjectMap<Player, PlayerUI> uis = new ObjectMap<Player, PlayerUI>();
    public int buttonsMenu;
    public int textInputMenu;

    public UI() {
        buttonsMenu = Menus.registerMenu((player, option) -> {
            uis.get(player, () -> new PlayerUI(player)).button(option);
        });
        textInputMenu = Menus.registerTextInput((player, option) -> {
            uis.get(player, () -> new PlayerUI(player)).text(option);
        });
    }

    public void playerJoin(Player player){
        uis.put(player, new PlayerUI(player));
    }

    public void playerLeave(Player player){
        uis.remove(player);
    }

    public void update(){
        uis.each((p, ui) -> {
            ui.update();
        });
    }

    public static UIMonad<Single> testScreen(){
        Seq<Seq<Pair<String, Prov<UIMonad<Single>>>>> a = new Seq<Seq<Pair<String, Prov<UIMonad<Single>>>>>();
        for(int i = 0; i < 5; i++){
            Seq<Pair<String, Prov<UIMonad<Single>>>> b = new Seq<Pair<String, Prov<UIMonad<Single>>>>();
            for(int j = 0; j < 5; j++){
                b.add(new Pair<String, Prov<UIMonad<Single>>>("Test " + i + " " + j, () -> {return UIMonad.pure(Single.uniq);}));
            }
            a.add(b);
        }
        return UIMonad.<Single>constructButtons(a, "Test", "Test");
    }

    public static UIMonad<Single> selectionScreen(Seq<Player> players){
        players.remove(p -> !Groups.player.contains(pp -> pp == p));
        return UIMonad.<Single>constructButtons(
                Groups.player.copy().map(p -> new Seq<Pair<String, Prov<UIMonad<Single>>>>().add(new Pair<String, Prov<UIMonad<Single>>>((players.contains(p) ? "[green]#[] " : "[scarlet]-[] ") + p.name, () -> {
                    if (players.contains(p)) {
                        players.remove(p);
                    } else {
                        players.add(p);
                    }
                    return selectionScreen(players);
                })))
                .add(new Seq<Pair<String, Prov<UIMonad<Single>>>>().add(new Pair<String, Prov<UIMonad<Single>>>("Exit", () -> {
                    for(var p : players){
                        Log.info(p.name);
                    }
                    return UIMonad.pure(Single.uniq);
                })))
                , "Select players", "Select your challengers");
    }

    public class PlayerUI {
        public Player player;
        public UIMonad<Single> state = UIMonad.pure(Single.uniq);

        public PlayerUI(Player player){
            this.player = player;
        }

        public <X> boolean request(UIMonad<X> newState){
            return state.get(a -> {
                this.state = newState.map(aa -> Single.uniq).strict();
                show();
                return true;
            }, a -> false, a -> false);
        }

        public void button(int option){
            if(option < 0) return;
            state.<Single>get(
                    a -> Single.uniq,
                    buttonss -> {
                        int i = 0;
                        for(var buttons : buttonss){
                            if(option - i < 0) break;
                            if(buttons.size > option - i){
                                state = buttons.get(option - i).b.get();
                                show();
                                return Single.uniq;
                            }
                            else {
                                i = i + buttons.size;
                            }
                        }
                        show();
                        return Single.uniq;
                    },
                    a -> {
                        show();
                        return Single.uniq;
                    }
            );
        }

        public void text(String text){
            state.get(a -> Single.uniq,
                    buttons -> {
                        show();
                        return Single.uniq;
                    },
                    textInput -> {
                        state = textInput.get(text);
                        show();
                        return Single.uniq;
                    }
            );
        }

        public void show(){
            state.get(a -> Single.uniq,
                    buttons -> {
                        String[][] items = new String[buttons.size][];
                        int i = 0;
                        for(var bs : buttons){
                            String[] itemss = new String[bs.size];
                            int j = 0;
                            for(var b : bs){
                                itemss[j++] = b.a;
                            }
                            items[i++] = itemss;
                        }
                        Call.menu(player.con, buttonsMenu, state.title, state.description, items);
                        return Single.uniq;
                    },
                    text -> {
                        Call.textInput(player.con, buttonsMenu, state.title, state.description, 1, "", false);
                        return Single.uniq;
                    }
            );
        }

        public void update(){
            if(!state.isPure()) return;
        }
    }
}
