package net.thucydides.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class used to get and set Thucydides system properties.
 */
public class ThucydidesSystemProperties {

    static ThucydidesSystemProperties currentSystemProperties = new ThucydidesSystemProperties();

    public static ThucydidesSystemProperties getProperties() {
        return currentSystemProperties;
    }

    public String getValue(ThucydidesSystemProperty property) {
        return System.getProperty(property.getPropertyName());
    }

    /**
     * Determines whether a Thucydides system property has been set or not.
     */
    public boolean isDefined(final ThucydidesSystemProperty property) {
        return (System.getProperty(property.getPropertyName()) != null);
    }

    public String getValue(final ThucydidesSystemProperty property, final String defaultValue) {
        return isDefined(property) ? getValue(property) : defaultValue;
    }

    /**
     * Determines whether or not a Thucydides system property has been set to a non-empty value.
     */
    public boolean isEmpty(final ThucydidesSystemProperty property) {
        String value = System.getProperty(property.getPropertyName());
        return (StringUtils.isEmpty(value));
    }

    /**
     * Sets a Thucydides system property to s specified value.
     */
    public void setValue(final ThucydidesSystemProperty property, final String value) {
        System.setProperty(property.getPropertyName(), value);
    }

    public Integer getIntegerValue(ThucydidesSystemProperty property, Integer defaultValue) {
        String value = System.getProperty(property.getPropertyName());
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return defaultValue;
        }
    }

    public Boolean getBooleanValue(ThucydidesSystemProperty property, boolean defaultValue) {
        String value = System.getProperty(property.getPropertyName());
        if (value != null) {
            return Boolean.valueOf(value);
        } else {
            return defaultValue;
        }
    }
}
