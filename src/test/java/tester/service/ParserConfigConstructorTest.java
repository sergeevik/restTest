package tester.service;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserConfigConstructorTest {

    @Test
    public void testRequestMapperInvoke()
    {
        Constructor<?>[] declaredConstructors = ParserConfig.class.getDeclaredConstructors();
        assertThat(declaredConstructors)
                .isNotNull()
                .hasSize(1);

        Constructor<?> constructor = declaredConstructors[0];
        assertThat(constructor.getModifiers())
                .isEqualTo(Modifier.PRIVATE);
    }
}
