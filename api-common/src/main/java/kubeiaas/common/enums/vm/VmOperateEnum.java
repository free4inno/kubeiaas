package kubeiaas.common.enums.vm;

/**
 * 虚拟机操作.
 */
public enum VmOperateEnum {

    STOP, START, REBOOT, SUSPEND, RESUME;

    public String toString() {
        switch (this) {
            case STOP:
                return "stop";
            case START:
                return "start";
            case REBOOT:
                return "reboot";
            case SUSPEND:
                return "suspend";
            case RESUME:
                return "resume";
        }
        return super.toString();
    }
}
