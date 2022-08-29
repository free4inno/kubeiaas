package kubeiaas.iaascore.utils;

import kubeiaas.common.bean.Volume;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 硬盘挂载点工具，为了简化代码复杂性，只考虑对第二块硬盘开始的（数据盘）的挂载点设置，对于
 * 系统盘，直接设置值.
 */
@Component
public class MountPointUtils {
    private final char limitMountPoint = 'z';
    private char maxUsedMountPoint = 'a';
    private Map<Character, Integer> usedPointList;  //当value为0时，表示未使用

    public MountPointUtils() {
        usedPointList = new HashMap<>();
        for (char a = 'a'; a <= 'z'; a++) {
            usedPointList.put(a, 0);
        }
    }

    public String getMountPoint(List<Volume> volumes) {
        refreshUsedList();
        maxUsedMountPoint = 'a';        //每次使用进行初始化
        for (Volume volume : volumes) {
            if (volume.getMountPoint() != null) {
                setUsedDevPoint(volume.getMountPoint());
            }
        }
        String newMountPoint = null;
        for (maxUsedMountPoint++; maxUsedMountPoint <= limitMountPoint; maxUsedMountPoint++) {
            if (usedPointList.get(maxUsedMountPoint) == 0) {
                newMountPoint = maxUsedMountPoint + "";
                return newMountPoint;
            }
        }
        return "";
    }

    private void refreshUsedList() {
        for (char a = 'a'; a <= 'z'; a++) {
            usedPointList.put(a, 0);
        }
    }

    //一般传递进来 vda、vdb 等
    private void setUsedDevPoint(String mountPoint) {
        if (mountPoint.length() < 3) {
            return;
        }
        char mount = mountPoint.charAt(2);
        usedPointList.put(mount, 1);
        if (maxUsedMountPoint < mount) {
            maxUsedMountPoint = mount;
        }
    }

}
