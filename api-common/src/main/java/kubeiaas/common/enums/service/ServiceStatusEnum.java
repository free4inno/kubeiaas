package kubeiaas.common.enums.service;

public enum ServiceStatusEnum {
    NON, DEAD, ACTIVE;

    public String toString(){
        switch (this) {
            case NON:
                return "no_need";
            case DEAD:
                return "dead";
            case ACTIVE:
                return "active";
        }
        return super.toString();
    }
}
