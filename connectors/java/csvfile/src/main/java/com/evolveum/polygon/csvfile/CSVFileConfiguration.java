/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2011 Viliam Repan (lazyman)
 * Portions Copyrighted 2011 - 2014 Evolveum
 *
 * $Id$
 */
package com.evolveum.polygon.csvfile;

import java.io.File;
import java.nio.charset.Charset;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * Extends the {@link AbstractConfiguration} class to provide all the necessary
 * parameters to initialize the CSVFile Connector.
 *
 * @author Viliam Repan (lazyman)
 * @author $author$
 * @version $Revision$ $Date$
 */
public class CSVFileConfiguration extends AbstractConfiguration {

    private static final Log log = Log.getLog(CSVFileConfiguration.class);
    // Exposed configuration properties.
    private File filePath = null;
    private String encoding = "utf-8";
    private String valueQualifier = "\"";
    private String fieldDelimiter = ",";
    private String multivalueDelimiter = ";";
    private boolean usingMultivalue = false;
    private String uniqueAttribute = null;
    private String nameAttribute = null;
    private String passwordAttribute = null;
    private boolean alwaysQualify = true;
    private int preserveLastTokens = 10;

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_FILE_PATH",
			helpMessageKey = "UI_FLAT_FILE_PATH_HELP", required = true, order = 1)
	public File getFilePath() {
		return filePath;
	}

	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_UNIQUE_ATTRIBUTE",
			helpMessageKey = "UI_FLAT_UNIQUE_ATTRIBUTE_HELP", required = true, order = 2)
	public String getUniqueAttribute() {
		return uniqueAttribute;
	}

	public void setUniqueAttribute(String uniqueAttribute) {
		this.uniqueAttribute = uniqueAttribute;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_NAME_ATTRIBUTE",
			helpMessageKey = "UI_FLAT_NAME_ATTRIBUTE_HELP", order = 3)
	public String getNameAttribute() {
		if (StringUtil.isEmpty(nameAttribute)) {
			return uniqueAttribute;
		}
		return nameAttribute;
	}

	public void setNameAttribute(String nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_PASSWORD_ATTRIBUTE",
			helpMessageKey = "UI_FLAT_PASSWORD_ATTRIBUTE_HELP", order = 4)
	public String getPasswordAttribute() {
		return passwordAttribute;
	}

	public void setPasswordAttribute(String passwordAttribute) {
		this.passwordAttribute = passwordAttribute;
	}

    @ConfigurationProperty(displayMessageKey = "UI_FLAT_FIELD_DELIMITER",
    helpMessageKey = "UI_FLAT_FIELD_DELIMITER_HELP", order = 5)
    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_FILE_ALWAYS_QUALIFY",
			helpMessageKey = "UI_FLAT_FILE_ALWAYS_QUALIFY_HELP", order = 6)
	public boolean getAlwaysQualify() {
		return alwaysQualify;
	}

	public void setAlwaysQualify(boolean alwaysQualify) {
		this.alwaysQualify = alwaysQualify;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_VALUE_QUALIFIER",
			helpMessageKey = "UI_FLAT_VALUE_QUALIFIER_HELP", order = 7)
	public String getValueQualifier() {
		return valueQualifier;
	}

	public void setValueQualifier(String valueQualifier) {
		this.valueQualifier = valueQualifier;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_USING_MULTIVALUE",
			helpMessageKey = "UI_FLAT_USING_MULTIVALUE_HELP", order = 8)
	public boolean isUsingMultivalue() {
		return usingMultivalue;
	}

	public void setUsingMultivalue(boolean usingMultivalue) {
		this.usingMultivalue = usingMultivalue;
	}

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_MULTIVALUE_DELIMITER",
    helpMessageKey = "UI_FLAT_MULTIVALUE_DELIMITER_HELP", order = 9)
    public String getMultivalueDelimiter() {
        return multivalueDelimiter;
    }

    public void setMultivalueDelimiter(String multivalueDelimiter) {
        this.multivalueDelimiter = multivalueDelimiter;
    }

	@ConfigurationProperty(displayMessageKey = "UI_FLAT_ENCODING",
			helpMessageKey = "UI_FLAT_ENCODING_HELP", order = 10)
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@ConfigurationProperty(displayMessageKey = "UI_PRESERVE_LAST_TOKENS",
			helpMessageKey = "UI_PRESERVE_LAST_TOKENS_HELP", order = 11)
	public int getPreserveLastTokens() {
		return preserveLastTokens;
	}

	public void setPreserveLastTokens(int preserveLastTokens) {
		this.preserveLastTokens = preserveLastTokens;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public void validate() {
        log.ok("begin");

        if (StringUtil.isEmpty(encoding)) {
            throw new ConfigurationException("Encoding is not defined.");
        }
        if (!Charset.isSupported(encoding)) {
            throw new ConfigurationException("Encoding '" + encoding + "' is not supported.");
        }

        if (StringUtil.isEmpty(fieldDelimiter)) {
            throw new ConfigurationException("Field delimiter can't be null or empty.");
        }

        if (StringUtil.isEmpty(valueQualifier)) {
            throw new ConfigurationException("Value qualifier can't be null or empty.");
        }

        if (StringUtil.isEmpty(multivalueDelimiter)) {
            throw new ConfigurationException("Multivalue delimiter delimiter can't be null or empty.");
        }

        if (fieldDelimiter.equals(valueQualifier)) {
            throw new ConfigurationException("Field delimiter '" + fieldDelimiter + "' can't be equal to value qualifier.");
        }

        if (StringUtil.isEmpty(uniqueAttribute)) {
            throw new ConfigurationException("Unique attribute is not defined.");
        }

        if (StringUtil.isEmpty(nameAttribute)) {
            log.warn("Name attribute not defined, value from unique attribute will be used (" + uniqueAttribute + ").");
        }

        if (StringUtil.isEmpty(passwordAttribute)) {
            log.warn("Password attribute is not defined.");
        }

        Assertions.nullCheck(filePath, "File path");

        if (!filePath.exists()) {
            throw new ConfigurationException("File '" + filePath + "' doesn't exists. At least file with csv header must exist.");
        }
        if (filePath.isDirectory()) {
            throw new ConfigurationException("File path '" + filePath + "' points to a directory.");
        }
        if (!filePath.canRead()) {
            throw new ConfigurationException("File '" + filePath + "' can't be read.");
        }
        if (!filePath.canWrite()) {
            throw new ConfigurationException("Can't write to file '" + filePath.getAbsolutePath() + "'.");
        }

        log.ok("end");
    }

    boolean isUniqueAndNameAttributeEqual() {
		String effectiveNameAttribute = getNameAttribute();
		return uniqueAttribute == null ? effectiveNameAttribute == null : uniqueAttribute.equals(effectiveNameAttribute);
    }
}
