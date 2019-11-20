package com.attach.core.util;

import com.attach.core.view.Ansi;

import java.io.File;

/**
 * @author: TuGai
 * @createTime: 2019-07-21 18:26
 **/
public interface Constants {


    /**
     * Spy的全类名
     */
    String SPY_CLASSNAME = "com.attach.agent.Spy";

    /**
     * 中断提示
     */
    String Q_OR_CTRL_C_ABORT_MSG = "Press Q or Ctrl+C to abort.";

    /**
     * 空字符串
     */
    String EMPTY_STRING = "";

    /**
     * 命令提示符
     */
    String DEFAULT_PROMPT = "$ ";

    /**
     * 带颜色命令提示符
     * raw string: "[33m$ [m"
     */
    String COLOR_PROMPT = Ansi.ansi().fg(Ansi.Color.YELLOW).a(DEFAULT_PROMPT).reset().toString();

    /**
     * 方法执行耗时
     */
    String COST_VARIABLE = "cost";

    String CMD_HISTORY_FILE = System.getProperty("user.home") + File.separator + ".arthas" + File.separator + "history";

    /**
     * 当前进程PID
     */
//    String PID = ApplicationUtils.getPid();

    /**
     * 缓存目录
     */
//    String CACHE_ROOT = LOGS_DIR + File.separator + "arthas-cache";

}
