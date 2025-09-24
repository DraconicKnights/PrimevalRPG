package com.primevalrpg.primeval.utils.ScriptingEngine;


import javax.script.ScriptEngine;


public class EngineHandler {
    private static EngineHandler instance;
    private ScriptEngine engine;

    public EngineHandler() {
        instance = this;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public static EngineHandler getInstance() {
        return instance;
    }

}
