package kubeiaas.common.commands;

public class CommonCommands {
    /* ************************************************************
     *                      Shell File Commands
     * ************************************************************/
    public static final String SHELL_IS_FILE_EXIST = "if [ -f %s ]; then echo '1'; else echo '0'; fi";
    public static final String SHELL_IS_DIR_EXIST = "if [ -d %s ]; then echo '1'; else echo '0'; fi";
    public static final String SHELL_GET_FILE_SIZE_IN_BYTES = "ls -l %s | awk '{print $5}'";
}
