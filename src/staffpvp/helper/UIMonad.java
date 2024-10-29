package staffpvp.helper;

import arc.util.*;
import arc.struct.*;
import arc.func.*;
import java.lang.*;

//TODO: may separate this into a library in the future? consider rewriting to Scala :P
/**
 * Church-encoded free monad of basic UI. <br>
 * Please abuse java's IO in the non-pure parts only. <br>
 * Thanks. <br>
 */
public class UIMonad<A> {
    public Inner<A> inner;
    public String title;
    public String description;

    public static interface Inner<A> {
        <X> X get(Func<A,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput);
    }

    public UIMonad(Inner<A> inner, String title, String description){
        this.inner = inner;
        this.title = title;
        this.description = description;
    }

    public <X> X get(Func<A,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput){
        return this.inner.get(pure, buttons, textInput);
    }

    public static <A> UIMonad<A> pure(A a){
        return constructPure(a, "", "");
    }

    public <B> UIMonad<B> map(Func<A,B> f){
        var _inner = inner;
        return new UIMonad<B>(new Inner<B>(){
            @Override
            public <X> X get(Func<B,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput) {
                return _inner.get(a -> pure.get(f.get(a)), buttons, textInput);
            };
        }, title, description);
    }

    public <B> UIMonad<B> flatMap(Func<A, UIMonad<B>> f){
        var _inner = inner;
        return new UIMonad<B>(new Inner<B>(){
            @Override
            public <X> X get(Func<B,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput) {
                return _inner.get(a -> f.get(a).get(pure, buttons, textInput), buttons, textInput);
            };
        }, title, description);
    }

    public boolean isPure(){
        return this.inner.get(a -> true, a -> false, a -> false);
    }

    public UIMonad<A> strict(){
        this.inner = this.inner.<UIMonad<A>>get(a -> constructPure(a, "", ""), b -> constructButtons(b, "", "").flatMap(a -> a), c -> constructTextInput(c, "", "").flatMap(a -> a)).inner;
        return this;
    }

    public static <A> UIMonad<A> constructPure(A a, String title, String description){
        return new UIMonad<A>(new Inner<A>(){
            @Override
            public <X> X get(Func<A,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput) {
                return pure.get(a);
            };
        }, title, description);
    }

    public static <A> UIMonad<A> constructButtons(Seq<Seq<Pair<String, Prov<UIMonad<A>>>>> btns, String title, String description){
        return new UIMonad<A>(new Inner<A>(){
            @Override
            public <X> X get(Func<A,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput) {
                return buttons.get(btns.map(l -> l.map(p -> new Pair<String, Prov<UIMonad<X>>>(p.a, () -> p.b.get().map(pure)) )));
            };
        }, title, description);
    }

    public static <A> UIMonad<A> constructTextInput(Func<String, UIMonad<A>> txtin, String title, String description){
        return new UIMonad<A>(new Inner<A>(){
            @Override
            public <X> X get(Func<A,X> pure, Func<Seq<Seq<Pair<String, Prov<UIMonad<X>>>>>,X> buttons, Func<Func<String,UIMonad<X>>,X> textInput) {
                return textInput.get(a -> txtin.get(a).map(pure));
            };
        }, title, description);
    }
}
