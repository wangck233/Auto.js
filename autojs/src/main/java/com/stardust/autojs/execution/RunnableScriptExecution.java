package com.stardust.autojs.execution;

import android.util.Log;

import com.stardust.autojs.engine.ScriptEngine;
import com.stardust.autojs.engine.ScriptEngineManager;
import com.stardust.autojs.runtime.ScriptInterruptedException;
import com.stardust.autojs.script.ScriptSource;

/**
 * Created by Stardust on 2017/5/1.
 */

public class RunnableScriptExecution extends ScriptExecution.AbstractScriptExecution implements Runnable {

    private static final String TAG = "RunnableJSExecution";
    private ScriptEngine mScriptEngine;
    private ScriptEngineManager mScriptEngineManager;

    public RunnableScriptExecution(ScriptEngineManager manager, ScriptExecutionTask task) {
        super(task);
        mScriptEngineManager = manager;
    }

    @Override
    public void run() {
        execute();
    }

    public Object execute() {
        mScriptEngine = mScriptEngineManager.createEngineOfSourceOrThrow(getSource());
        return execute(mScriptEngine);
    }

    private Object execute(ScriptEngine engine) {
        try {
            prepare(engine);
            return doExecution(engine);
        } catch (Exception e) {
            e.printStackTrace();
            getListener().onException(this, e);
        } finally {
            Log.d(TAG, "Engine destroy");
            engine.destroy();
        }
        return null;
    }

    private void prepare(ScriptEngine engine) {
        engine.setTag(ScriptEngine.TAG_PATH, getConfig().getPath());
        engine.init();
    }

    private Object doExecution(ScriptEngine engine) {
        engine.setTag(ScriptEngine.TAG_SOURCE, getSource());
        getListener().onStart(this);
        Object result = null;
        long delay = getConfig().delay;
        int times = getConfig().loopTimes;
        if (times == 0) {
            times = Integer.MAX_VALUE;
        }
        long interval = getConfig().interval;
        sleep(delay);
        ScriptSource source = getSource();
        for (int i = 0; i < times; i++) {
            result = engine.execute(source);
            sleep(interval);
        }
        getListener().onSuccess(this, result);
        return result;
    }

    private void sleep(long i) {
        if (i <= 0) {
            return;
        }
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new ScriptInterruptedException();
        }
    }

    @Override
    public ScriptEngine getEngine() {
        return mScriptEngine;
    }

}