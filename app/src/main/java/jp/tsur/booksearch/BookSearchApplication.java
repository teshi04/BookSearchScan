package jp.tsur.booksearch;

import android.app.Application;

import dagger.ObjectGraph;


public class BookSearchApplication extends Application {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(getModules());
        graph.inject(this);
    }

    protected Object getModules() {
        return new BookSearchModule(this);
    }

    ObjectGraph getObjectGraph() {
        return graph;
    }
}
