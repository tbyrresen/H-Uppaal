package SW9.abstractions;

import SW9.HUPPAAL;
import SW9.backend.BackendException;
import SW9.backend.UPPAALDriver;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import com.uppaal.engine.Engine;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.util.function.Consumer;

public class Query implements Serializable {
    private static final String QUERY = "query";
    private static final String COMMENT = "comment";

    private final ObjectProperty<QueryState> queryState = new SimpleObjectProperty<>(QueryState.UNKNOWN);
    private final StringProperty query = new SimpleStringProperty("");
    private final StringProperty comment = new SimpleStringProperty("");

    private Runnable runQuery;

    public Query(final String query, final String comment, final QueryState queryState) {
        this.query.set(query);
        this.comment.set(comment);
        this.queryState.set(queryState);

        initializeRunQuery();
    }

    public Query(final JsonObject jsonElement) {
        deserialize(jsonElement);

        initializeRunQuery();
    }

    public QueryState getQueryState() {
        return queryState.get();
    }

    public void setQueryState(final QueryState queryState) {
        this.queryState.set(queryState);
    }

    public ObjectProperty<QueryState> queryStateProperty() {
        return queryState;
    }

    public String getQuery() {
        return query.get();
    }

    public void setQuery(final String query) {
        this.query.set(query);
    }

    public StringProperty queryProperty() {
        return query;
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(final String comment) {
        this.comment.set(comment);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    private void initializeRunQuery() {
        final Engine[] engine = {null};
        final Boolean[] forcedCancel = {false};

        runQuery = () -> {
            if (getQueryState().equals(QueryState.RUNNING)) {
                synchronized (UPPAALDriver.engineLock) {
                    if(engine[0] != null) {
                        forcedCancel[0] = true;
                        engine[0].cancel();
                    }
                }
                setQueryState(QueryState.UNKNOWN);
            } else {
                setQueryState(QueryState.RUNNING);

                final Component mainComponent = HUPPAAL.getProject().getMainComponent();

                if (mainComponent == null) {
                    return; // We cannot generate a UPPAAL file without a main component
                }

                try {
                    UPPAALDriver.buildHUPPAALDocument();
                    UPPAALDriver.runQuery(getQuery(),
                            aBoolean -> {
                                if (aBoolean) {
                                    setQueryState(QueryState.SUCCESSFUL);
                                } else {
                                    setQueryState(QueryState.ERROR);
                                }
                            },
                            e -> {
                                if (!forcedCancel[0]) {
                                    setQueryState(QueryState.SYNTAX_ERROR);
                                }
                            },
                            eng -> {
                                engine[0] = eng;
                            }
                    ).start();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(QUERY, getQuery());
        result.addProperty(COMMENT, getComment());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setQuery(json.getAsJsonPrimitive(QUERY).getAsString());
        setComment(json.getAsJsonPrimitive(COMMENT).getAsString());
    }

    public void run() {
        runQuery.run();
    }
}
