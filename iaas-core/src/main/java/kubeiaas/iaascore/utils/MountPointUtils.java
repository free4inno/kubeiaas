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
    private static final char limitMountPoint = 'z';
    private static final char startMountPoint = 'a';
    private final Map<Character, Integer> usedPointMap;  //当value为0时，表示未使用

    public MountPointUtils() {
        usedPointMap = new HashMap<>();
        for (char a = 'a'; a <= 'z'; a++) {
            usedPointMap.put(a, 0);
        }
    }

    public String getMountPoint(List<Volume> volumes) {
        refreshUsedList();
        for (Volume volume : volumes) {
            if (volume.getMountPoint() != null) {
                setUsedDevPoint(volume.getMountPoint());
            }
        }
        char newMountPoint;
        for (newMountPoint = startMountPoint; newMountPoint <= limitMountPoint; newMountPoint++) {
            if (usedPointMap.get(newMountPoint) == 0) {
                return String.valueOf(newMountPoint);
            }
        }
        return "";
    }

    private void refreshUsedList() {
        for (char a = 'a'; a <= 'z'; a++) {
            usedPointMap.put(a, 0);
        }
    }

    //一般传递进来 vda、vdb 等
    private void setUsedDevPoint(String mountPoint) {
        if (mountPoint.length() < 3) {
            return;
        }
        char mount = mountPoint.charAt(2);
        usedPointMap.put(mount, 1);
    }

}
