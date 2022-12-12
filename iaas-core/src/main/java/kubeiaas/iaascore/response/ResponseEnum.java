package kubeiaas.iaascore.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OpenAPI 返回值状态对应的响应码和信息
 */
@Getter
@AllArgsConstructor
public enum ResponseEnum {
    SUCCESS(200f, "业务处理成功"),
    WORK_ERROR(202f, "业务处理失败"),
    VM_DELETE_ERROR(202.01f,"虚拟机删除失败"),
    VM_MODIFY_ERROR(202.02F,"虚拟机修改CPU/内存失败"),
    VM_START_ERROR(202.03F,"虚拟机启动失败"),
    VM_STOP_ERROR(202.04F,"虚拟机停止失败"),
    VM_REBOOT_ERROR(202.05F,"虚拟机重启失败"),
    VM_SUSPEND_ERROR(202.06F,"虚拟机暂停失败"),
    VM_RESUME_ERROR(202.07F,"虚拟机恢复失败"),
    PUBLISH_IMAGE_ERROR(202.08F,"云镜像发布失败"),
    VOLUME_DELETE_ERROR(202.11f,"云硬盘删除失败"),
    VOLUME_ATTACH_ERROR(202.12f,"云硬盘挂载失败"),
    VOLUME_DETACH_ERROR(202.13f,"云硬盘卸载失败"),
    ARGS_ERROR(402f, "验参失败"),
    INTERNAL_ERROR(500f, "内部错误");

    private final Float code;
    private final String msg;
}
