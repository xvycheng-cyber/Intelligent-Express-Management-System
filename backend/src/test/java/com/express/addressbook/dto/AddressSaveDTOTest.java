package com.express.addressbook.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressSaveDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void oneChineseCharacterIsValid() {
        assertThat(validate("王")).isEmpty();
    }

    @Test
    void fiftyBmpCharactersAreValid() {
        assertThat(validate("张".repeat(50))).isEmpty();
    }

    @Test
    void fiftyOneBmpCharactersAreInvalid() {
        assertThat(validate("张".repeat(51))).isNotEmpty();
    }

    @Test
    void fiftySupplementaryCharactersAreValid() {
        assertThat(validate("𠀀".repeat(50))).isEmpty();
    }

    @Test
    void fiftyOneSupplementaryCharactersAreInvalid() {
        assertThat(validate("𠀀".repeat(51))).isNotEmpty();
    }

    @Test
    void address255IsValid() {
        AddressSaveDTO dto = new AddressSaveDTO("王", "13800138000", "浙".repeat(255), 1);
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void address256IsInvalid() {
        AddressSaveDTO dto = new AddressSaveDTO("王", "13800138000", "浙".repeat(256), 1);
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void phone21IsInvalid() {
        AddressSaveDTO dto = new AddressSaveDTO("王", "1".repeat(21), "浙江省杭州市", 1);
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void commonRawPhoneFormatsAreAccepted() {
        assertThat(validator.validate(new AddressSaveDTO("王", "+86 138-0013-8000", "浙江省杭州市", 1))).isEmpty();
        assertThat(validator.validate(new AddressSaveDTO("王", "（13800138000）", "浙江省杭州市", 1))).isEmpty();
        assertThat(validator.validate(new AddressSaveDTO("王", "138 0013 8000", "浙江省杭州市", 1))).isEmpty();
    }

    @Test
    void fullWidthEnglishNameIsAccepted() {
        assertThat(validator.validate(new AddressSaveDTO("Ｊｏｈｎ", "13800138000", "浙江省杭州市", 1))).isEmpty();
    }

    private java.util.Set<jakarta.validation.ConstraintViolation<AddressSaveDTO>> validate(String name) {
        AddressSaveDTO dto = new AddressSaveDTO(name, "13800138000",
                "浙江省杭州市西湖区XX小区", 1);
        return validator.validate(dto);
    }
}
