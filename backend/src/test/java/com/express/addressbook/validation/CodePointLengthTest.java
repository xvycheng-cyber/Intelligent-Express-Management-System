package com.express.addressbook.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodePointLengthTest {

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
    void oneCodePointIsValid() {
        assertThat(validator.validate(new NameForm("王"))).isEmpty();
    }

    @Test
    void fiftySupplementaryCodePointsAreValid() {
        NameForm form = new NameForm("𠀀".repeat(50));
        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void fiftyOneSupplementaryCodePointsAreInvalid() {
        NameForm form = new NameForm("𠀀".repeat(51));
        assertThat(validator.validate(form)).hasSize(1);
    }

    @Test
    void fiftyOneBmpCodePointsAreInvalid() {
        assertThat(validator.validate(new NameForm("张".repeat(51)))).hasSize(1);
    }

    @Test
    void fiftyBmpCodePointsAreValid() {
        assertThat(validator.validate(new NameForm("张".repeat(50)))).isEmpty();
    }

    static class NameForm {

        @CodePointLength(min = 1, max = 50, message = "姓名长度必须为 1～50 个 Unicode 码点")
        private final String name;

        NameForm(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
