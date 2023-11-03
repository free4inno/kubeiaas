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
        log.info(String.format("specConfigSave info -- id: %d, type: %s, value: %s, description: %s", id, type, value, description));
        // 1. check Enum param
        log.info("STEP 1: check Enum param");
        SpecTypeEnum specTypeEnum = EnumUtils.getEnumFromString(SpecTypeEnum.class, type);
        if (specTypeEnum == null) {
            log.error("ERROR: unknown TYPE");
            throw new BaseException("err: unknown TYPE " + type, ResponseEnum.ARGS_ERROR);
        }
        // 2. build and save
        log.info("STEP 2: build and save");
        SpecConfig newSpecConfig = new SpecConfig(id, specTypeEnum, value, description);
        try {
            return tableStorage.specConfigSave(newSpecConfig);
        } catch (Exception e) {
            log.error("ERROR: DB save specConfig failed.");
            throw new BaseException("err: DB save specConfig failed.");
        }
    }

    public void specConfigDelete(Integer id) throws BaseException {
        log.info(String.format("specConfigDelete info -- id: %d", id));
        try {
            log.info("-> invoke DB -- specConfigDelete");
            tableStorage.specConfigDelete(id);
            log.info("<- invoke DB -- done");
        } catch (Exception e) {
            log.error(String.format("ERROR: DB delete failed. id: %s", id));
            throw new BaseException(String.format("err: DB delete failed. id: %s", id));
        }
    }
}
