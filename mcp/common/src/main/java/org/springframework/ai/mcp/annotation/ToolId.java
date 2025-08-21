package org.springframework.ai.mcp.annotation;

import java.lang.annotation.*;

/**
 * @author kazaff
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolId {

	String value();

	String category() default "general";

}
