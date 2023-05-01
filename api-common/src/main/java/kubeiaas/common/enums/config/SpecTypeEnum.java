package kubeiaas.common.enums.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public enum SpecTypeEnum {
    VM_COMPUTE("VM_COMPUTE"),
    SYS_VOLUME("SYS_VOLUME"),
    DATA_VOLUME("DATA_VOLUME"),
    VM_STATUS("VM_STATUS"),
    VOLUME_STATUS("VOLUME_STATUS"),
    NETWORK_TYPE("NETWORK_TYPE"),
    VNC_DOMAIN("VNC_DOMAIN");

    SpecTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }

    public static JSONArray toJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (SpecTypeEnum e : SpecTypeEnum.values()) {
            JSONObject object = new JSONObject();
            object.put("type", e.getType());
            jsonArray.add(object);
        }
        return jsonArray;
    }

}
