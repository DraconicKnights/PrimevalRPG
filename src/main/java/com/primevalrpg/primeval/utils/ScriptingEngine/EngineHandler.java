package com.primevalrpg.primeval.utils.ScriptingEngine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EngineHandler {
    private static EngineHandler instance;
    private ScriptEngine engine;

    public EngineHandler() {
        instance = this;
        engine = new ScriptEngineManager().getEngineByName("graal.js");
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public static EngineHandler getInstance() {
        return instance;
    }

}
