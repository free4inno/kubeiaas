package kubeiaas.dbproxy.table;

import kubeiaas.common.bean.Image;
import kubeiaas.common.enums.image.*;

import javax.persistence.*;

@Entity
@Table(name = "image")
public class ImageTable extends Image {

    public ImageTable() {
        super();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Integer getId() {
        return super.getId();
    }

    public void setId(Integer id) {
        super.setId(id);
    }

    @Column(name = "uuid")
    public String getUuid() {
        return super.getUuid();
    }

    public void setUuid(String uuid) {
        super.setUuid(uuid);
    }


    @Column(name = "name")
    public String getName() {
        return super.getName();
    }

    public void setName(String name) {
        super.setName(name);
    }

    @Column(name = "description")
    public String getDescription() {
        return super.getDescription();
    }

    public void setDescription(String description) {
        super.setDescription(description);
    }


    @Column(name = "directory")
    public String getDirectory() {
        return super.getDirectory();
    }

    public void setDirectory(String directory) {
        super.setDirectory(directory);
    }

    @Column(name = "format")
    @Enumerated(EnumType.STRING)
    public ImageFormatEnum getFormat() {
        return super.getFormat();
    }

    public void setFormat(ImageFormatEnum format) {
        super.setFormat(format);
    }

    @Column(name = "size")
    public Float getSize() {
        return super.getSize();
    }

    public void setSize(Float size) {
        super.setSize(size);
    }

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public ImageStatusEnum getStatus() {
        return super.getStatus();
    }

    public void setStatus(ImageStatusEnum status) {
        super.setStatus(status);
    }

    @Column(name = "min_mem")
    public Integer getMinMem() {
        return super.getMinMem();
    }

    public void setMinMem(Integer minMem) {
        super.setMinMem(minMem);
    }

    @Column(name = "min_disk")
    public Integer getMinDisk() {
        return super.getMinDisk();
    }

    public void setMinDisk(Integer minDisk) {
        super.setMinDisk(minDisk);
    }

    @Column(name = "os_type")
    @Enumerated(EnumType.STRING)
    public ImageOSTypeEnum getOsType() {
        return super.getOsType();
    }

    public void setOsType(ImageOSTypeEnum osType) {
        super.setOsType(osType);
    }

    @Column(name = "os_arch")
    @Enumerated(EnumType.STRING)
    public ImageOSArchEnum getOsArch() {
        return super.getOsArch();
    }

    public void setOsArch(ImageOSArchEnum osArch) {
        super.setOsArch(osArch);
    }

    @Column(name = "os_mode")
    @Enumerated(EnumType.STRING)
    public ImageOSModeEnum getOsMode() {
        return super.getOsMode();
    }

    public void setOsMode(ImageOSModeEnum osArch) {
        super.setOsMode(osArch);
    }

    @Column(name = "parent_image_uuid")
    public String getParentImageUuid() {
        return super.getParentImageUuid();
    }

    public void setParentImageUuid(String parentImageUuid) {
        super.setParentImageUuid(parentImageUuid);
    }
}
