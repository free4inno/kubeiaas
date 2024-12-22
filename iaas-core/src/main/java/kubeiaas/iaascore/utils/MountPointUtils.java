package kubeiaas.iaascore.utils;

import kubeiaas.common.bean.Volume;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 硬盘挂载点工具，不保存挂载状态，
 * 为了简化代码复杂性，只考虑对第二块硬盘（第一块硬盘是系统盘）开始的（数据盘）的挂载点设置，对于
 * 系统盘，直接设置值.
 */
@Component
public class MountPointUtils {
    private static final char limitMountPoint = 'z';
    private static final char startMountPoint = 'a';
    private final Map<Character, Integer> usedPointMap;  //当value为0时，表示未使用

    public MountPointUtils() {
        usedPointMap = new HashMap<>();
        for (char a = startMountPoint; a <= limitMountPoint; a++) {
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

    public Optional<String> getFirstAvailableMountPoint(List<String> mountedPoints) {
        refreshUsedList();
        for (String mountedPoint : mountedPoints) {
            if (mountedPoint != null && mountedPoint.length() > 2) {
                setUsedDevPoint(mountedPoint);
            }
        }
        char newMountPoint;
        for (newMountPoint = startMountPoint; newMountPoint <= limitMountPoint; newMountPoint++) {
            if (usedPointMap.get(newMountPoint) == 0) {
                return Optional.of(String.valueOf(newMountPoint));
            }
        }
        return Optional.empty();
    }

    public List<String> getAllAvailableMountPoint(List<String> mountedPoints) {
        refreshUsedList();
        for (String mountedPoint : mountedPoints) {
            if (mountedPoint != null && mountedPoint.length() > 2) {
                setUsedDevPoint(mountedPoint);
            }
        }
        return usedPointMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(entry -> String.valueOf(entry.getKey()))
                .collect(Collectors.toList());
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
