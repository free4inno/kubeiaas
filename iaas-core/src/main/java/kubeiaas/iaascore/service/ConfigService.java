package kubeiaas.iaascore.service;

import kubeiaas.common.bean.SpecConfig;
import kubeiaas.common.enums.config.SpecTypeEnum;
import kubeiaas.common.utils.EnumUtils;
import kubeiaas.iaascore.dao.TableStorage;
import kubeiaas.iaascore.exception.BaseException;
import kubeiaas.iaascore.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class ConfigService {
    @Resource
    private TableStorage tableStorage;

    public SpecConfig specConfigSave(Integer id, String type, String value, String description) throws BaseException {
        // 1. check Enum param
        SpecTypeEnum specTypeEnum = EnumUtils.getEnumFromString(SpecTypeEnum.class, type);
        if (specTypeEnum == null) {
            log.info("create ==== error ====");
            throw new BaseException("err: unknown TYPE " + type, ResponseEnum.ARGS_ERROR);
        }
        // 2. build and save
        SpecConfig newSpecConfig = new SpecConfig(id, specTypeEnum, value, description);
        try {
            return tableStorage.specConfigSave(newSpecConfig);
        } catch (Exception e) {
            log.info("create ==== error ====");
            throw new BaseException("err: DB save specConfig failed.");
        }
    }

    public void specConfigDelete(Integer id) throws BaseException {
        try {
            tableStorage.specConfigDelete(id);
        } catch (Exception e) {
            throw new BaseException("err: DB delete failed. id: " + id);
        }
    }
}
