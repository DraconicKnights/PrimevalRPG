package com.primevalrpg.primeval.utils.ScriptingEngine;


import com.primevalrpg.primeval.utils.Logger.RPGLogger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;


public class EngineHandler {
    private static EngineHandler instance;
    private ScriptEngine engine;

    public EngineHandler() {
        instance = this;

        List<ScriptEngineFactory> engines = new ScriptEngineManager().getEngineFactories();

        if (engines.isEmpty()) {
            RPGLogger.get().error("No Scripting Engine Factories Found!");
        }

        for (ScriptEngineFactory f : engines) {
            RPGLogger.get().info("Found scripting engine: " + f.getEngineName());
        }
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public static EngineHandler getInstance() {
        return instance;
    }

}
