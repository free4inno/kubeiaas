package kubeiaas.iaascore.utils;

import java.util.Arrays;
import java.util.List;

public class ConfigUtils {

    public static List<String> splitByComma(String str) {
        return Arrays.asList(str.split(","));
    }

}
