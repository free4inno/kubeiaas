package kubeiaas.common.bean;

import kubeiaas.common.enums.vm.VmStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vm {
    private int id;
    private String uuid;
    private String name;
    private String description;
    private String password;

    /* basic info */
    private int cpus;
    private int memory;
    private int diskSize;

    private String vncPort;
    private String vncPassword;

    private VmStatusEnum status;

    /* related */
    private String hostUuid;
    private String imageUuid;

    /* timestamp */
    private Timestamp createTime;

    /* !NOT_IN_DB */
    private List<IpUsed> ips;
}
