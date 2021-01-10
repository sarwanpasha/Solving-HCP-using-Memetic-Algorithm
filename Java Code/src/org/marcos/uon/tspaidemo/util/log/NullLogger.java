package org.marcos.uon.tspaidemo.util.log;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;

import java.util.AbstractList;
import java.util.Collection; 

/**
 * A stub-like logger that does nothing with it's inputs, is always empty, and has no threading impacts; This can be used to disable logging parametrically with minimal changes to the algorithm.
 * The idea is to you pass to the logger raw-form data at minimal cost to collect, and implement processing of the data inside a custom logger.
 * Then assigning the null logger can disable the majority of the performance impact of logging with minimal code changes. (And the runtime JIT compiler might completely optimise the call away, YMMV).
 */
public class NullLogger<T> implements ILogger<T> {
    public static class NullView<T> extends AbstractList<T> implements ILogger.View<T> {
        @Override
        public T get(int index) throws IndexOutOfBoundsException {
            throw new IndexOutOfBoundsException("This is a null view, there are no contents");
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean update() throws InterruptedException {
            return false;
        }

        @Override
        public boolean tryUpdate() throws InterruptedException {
            return false;
        }

        @Override
        public boolean isValid() throws InterruptedException {
            return false;
        }

        @Override
        public JsonElement jsonify() throws InterruptedException {
            JsonObject result = new JsonObject();
            result.add("states", new JsonArray());
            return result;
        }
    }

    public static <T> NullView<T> nullView() {
        return new NullView<>();
    }


    @Override
    public View<T> newView() throws InterruptedException {
        return nullView();
    }

    @Override
    public void log(T state) throws InterruptedException {
    }

    @Override
    public void logAll(Collection<T> states) throws InterruptedException {
    }

    @Override
    public void reset() {
    }

    @Override
    public void loadJson(JsonElement data) throws InterruptedException {
    }
}
