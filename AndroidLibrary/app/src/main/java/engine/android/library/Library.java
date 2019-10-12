package engine.android.library;

import engine.android.library.Library.Function.Callback;

public class Library {

    /**
     * 函数调用
     */
    public static <IN, OUT> void callFunction(Function<IN, OUT> function, IN param, Callback<OUT> callback) {
        try {
            function.doFunction(param, callback);
        } catch (Exception e) {
            callback.doError(e);
        }
    }

    public interface Function<IN, OUT> {

        void doFunction(IN params, Callback<OUT> callback);

        interface Callback<OUT> {

            void doResult(OUT result);

            void doError(Throwable e);
        }
    }
}