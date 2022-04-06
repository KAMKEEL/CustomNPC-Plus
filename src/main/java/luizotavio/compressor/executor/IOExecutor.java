package luizotavio.compressor.executor;

import java.util.concurrent.ExecutorService;

public class IOExecutor {

    /**
     * Please, do not change this value unless you know what you are doing.
     * This value is used to define the number of threads that will be used to
     * compress the files.
     *
     * If you want to change this value, please, do it with care.
     */
    public static ExecutorService IO_EXECUTOR;

}
