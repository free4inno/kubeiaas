package kubeiaas.common.enums.image;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum ImageUploadFormatEnum {
    IMAGE("img"), ISO("iso"), QCOW2("qcow2");

    private final String format;

    private ImageUploadFormatEnum(String format) {
        this.format = format;
    }

    public static Optional<ImageUploadFormatEnum> fromString(String format) {
        for (ImageUploadFormatEnum imageUploadFormatEnum : ImageUploadFormatEnum.values()) {
            if (imageUploadFormatEnum.getFormat().equalsIgnoreCase(format)) {
                return Optional.of(imageUploadFormatEnum);
            }
        }
        return Optional.empty();
    }

    public static boolean isEqual(ImageUploadFormatEnum uploadFormatEnum, ImageFormatEnum formatEnum) {
        return uploadFormatEnum.name().equalsIgnoreCase(formatEnum.name());
    }

    public static boolean isMatch(ImageUploadFormatEnum uploadFormatEnum, ImageFormatEnum formatEnum) {
        switch (uploadFormatEnum) {
            case IMAGE:
                return formatEnum == ImageFormatEnum.IMAGE || formatEnum == ImageFormatEnum.QCOW2;
            case ISO:
                return formatEnum == ImageFormatEnum.ISO;
            case QCOW2:
                return formatEnum == ImageFormatEnum.QCOW2;
            default:
                return false;
        }
    }
}
