package staffpvp.helper;

import arc.func.*;

/** Equivalent of the disjoint union of two types. */
public class Either<A,B> {
    public Inner<A,B> inner;

    public static interface Inner<A,B> {
        <C> C get(Func<A,C> f1, Func<B,C> f2);
    }

    public <C> C get(Func<A,C> f1, Func<B,C> f2){
        return inner.get(f1, f2);
    }

    public Either(Inner<A,B> inner){
    }

    public static <A,B> Either<A,B> left(A a){
        return new Either<A,B>(new Inner<A,B>(){
            @Override
            public <C> C get(Func<A,C> f1, Func<B,C> f2){
                return f1.get(a);
            }
        });
    }

    public static <A,B> Either<A,B> right(B b){
        return new Either<A,B>(new Inner<A,B>(){
            @Override
            public <C> C get(Func<A,C> f1, Func<B,C> f2){
                return f2.get(b);
            }
        });
    }

    public <C> Either<C,B> mapLeft(Func<A,C> f){
        return mapBoth(f, b -> b);
    }

    public <C> Either<A,C> mapRight(Func<B,C> f){
        return mapBoth(a -> a, f);
    }

    public <C,D> Either<C,D> mapBoth(Func<A,C> f, Func<B,D> g){
        var _inner = this.inner;
        return new Either<C,D>(new Inner<C,D>(){
            @Override
            public <E> E get(Func<C,E> f1, Func<D,E> f2){
                return _inner.get(a -> f1.get(f.get(a)), b -> f2.get(g.get(b)));
            }
        });
    }

    public Either<A,B> mapLeftMut(Func<A,A> f){
        return mapBothMut(f, b -> b);
    }

    public Either<A,B> mapRightMut(Func<B,B> f){
        return mapBothMut(a -> a, f);
    }

    public Either<A,B> mapBothMut(Func<A,A> f, Func<B,B> g){
        var _inner = this.inner;
        this.inner = new Inner<A,B>(){
            @Override
            public <C> C get(Func<A,C> f1, Func<B,C> f2){
                return _inner.get(a -> f1.get(f.get(a)), b -> f2.get(g.get(b)));
            }
        };
        return this;
    }

    public <A> A fuse(Either<A,A> x){
        return x.get(a -> a, a -> a);
    }

    public Either<A,B> strict(){
        this.inner = this.inner.get(a -> ((Either<A,B>) left(a)).inner, b -> ((Either<A,B>) right(b)).inner);
        return this;
    }
}
