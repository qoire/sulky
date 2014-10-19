/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2014 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.version;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
 */
public class JavaVersion
	implements Comparable<JavaVersion>
{
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+)([_](\\d+))?)?(-(.+))?");

	private static final int HUGE_GROUP_INDEX  = 1;
	private static final int MAJOR_GROUP_INDEX = 2;
	private static final int MINOR_GROUP_INDEX = 4;
	private static final int PATCH_GROUP_INDEX = 6;
	private static final int IDENTIFIER_GROUP_INDEX = 8;

	/*
	 e.g. 1.8.0_25
	 */
	private static final String JAVA_VERSION_PROPERTY_NAME = "java.version";

	/*
	 e.g. 1.8
	 */
	private static final String JAVA_SPECIFICATION_VERSION_PROPERTY_NAME = "java.specification.version";

	/**
	 * Smallest possible version is JavaVersion(0,0,0,0).
	 */
	public static final JavaVersion MIN_VALUE = new JavaVersion(0,0,0,0);

	/**
	 * The best possible approximation to the JVM JavaVersion.
	 *
	 * This can
	 */
	public static final JavaVersion JVM;

	static
	{
		JavaVersion version = getSystemJavaVersion();

		if(version == null)
		{
			// couldn't obtain any version info
			version = MIN_VALUE;
		}

		JVM=version;
	}

	static JavaVersion getSystemJavaVersion() {
		JavaVersion version=null;
		try
		{
			String versionString = System.getProperty(JAVA_VERSION_PROPERTY_NAME);
			if(versionString != null)
			{
				version = parse(versionString);
			}
		}
		catch(SecurityException ex)
		{
			// ignore
		}
		catch(IllegalArgumentException ex)
		{
			// didn't parse. Probably some strangeness like 1.8.0_25.1
		}

		if(version == null)
		{
			// either SecurityException or missing/broken standard property
			// fall back to specification version
			try
			{
				String versionString = System.getProperty(JAVA_SPECIFICATION_VERSION_PROPERTY_NAME);
				if(versionString != null)
				{
					version = parse(versionString);
				}
			}
			catch(SecurityException ex)
			{
				// ignore
			}
			catch(IllegalArgumentException ex)
			{
				// didn't parse.
			}
		}
		return version;
	}

	public static JavaVersion parse(String versionString)
	{
		if(versionString == null)
		{
			throw new IllegalArgumentException("versionString must not be null!");
		}
		Matcher matcher = VERSION_PATTERN.matcher(versionString);
		if(!matcher.matches())
		{
			throw new IllegalArgumentException("versionString '"+versionString+"' is invalid.");
		}

		/*
		for (int i=0; i<=matcher.groupCount(); i++)
		{
			System.out.println("Index #"+i+": "+matcher.group(i));
		}
		*/

		int huge = Integer.parseInt(matcher.group(HUGE_GROUP_INDEX));
		int major = Integer.parseInt(matcher.group(MAJOR_GROUP_INDEX));
		int minor = 0;
		int patch = 0;

		String minorString = matcher.group(MINOR_GROUP_INDEX);
		if(minorString != null)
		{
			minor = Integer.parseInt(minorString);
		}
		String patchString = matcher.group(PATCH_GROUP_INDEX);
		if(patchString != null)
		{
			patch = Integer.parseInt(patchString);
		}

		String identifier = matcher.group(IDENTIFIER_GROUP_INDEX);
		return new JavaVersion(huge, major, minor, patch, identifier);
	}

	private final int huge;
	private final int major;
	private final int minor;
	private final int patch;
	private final String identifier;

	public JavaVersion(int huge, int major)
	{
		this(huge, major, 0, 0, null);
	}

	public JavaVersion(int huge, int major, int minor)
	{
		this(huge, major, minor, 0, null);
	}

	public JavaVersion(int huge, int major, int minor, int patch)
	{
		this(huge, major, minor, patch, null);
	}

	public JavaVersion(int huge, int major, int minor, int patch, String identifier)
	{
		if(huge < 0)
		{
			throw new IllegalArgumentException("huge must not be negative!");
		}
		if(major < 0)
		{
			throw new IllegalArgumentException("major must not be negative!");
		}
		if(minor < 0)
		{
			throw new IllegalArgumentException("minor must not be negative!");
		}
		if(patch < 0)
		{
			throw new IllegalArgumentException("patch must not be negative!");
		}
		if(identifier != null && identifier.length() == 0)
		{
			throw new IllegalArgumentException("identifier must not be empty string!");
		}

		this.huge = huge;
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.identifier = identifier;
	}

	public int getHuge()
	{
		return huge;
	}

	public int getMajor()
	{
		return major;
	}

	public int getMinor()
	{
		return minor;
	}

	public int getPatch()
	{
		return patch;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String toVersionString()
	{
		StringBuilder result = new StringBuilder();
		result.append(huge).append('.').append(major).append('.').append(minor);
		if(patch != 0)
		{
			result.append('_');
			if(patch < 10)
			{
				result.append('0');
			}
			result.append(patch);
		}
		if(identifier != null)
		{
			result.append('-');
			result.append(identifier);
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JavaVersion that = (JavaVersion) o;

		if (huge != that.huge) return false;
		if (major != that.major) return false;
		if (minor != that.minor) return false;
		if (patch != that.patch) return false;
		if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = huge;
		result = 31 * result + major;
		result = 31 * result + minor;
		result = 31 * result + patch;
		result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "JavaVersion{" +
				"huge=" + huge +
				", major=" + major +
				", minor=" + minor +
				", patch=" + patch +
				", identifier='" + identifier + '\'' +
				'}';
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public int compareTo(JavaVersion other)
	{
		if(other == null)
		{
			throw new NullPointerException("other must not be null!");
		}
		if(huge < other.huge)
		{
			return -1;
		}
		if(huge > other.huge)
		{
			return 1;
		}
		if(major < other.major)
		{
			return -1;
		}
		if(major > other.major)
		{
			return 1;
		}
		if(minor < other.minor)
		{
			return -1;
		}
		if(minor > other.minor)
		{
			return 1;
		}
		if(patch < other.patch)
		{
			return -1;
		}
		if(patch > other.patch)
		{
			return 1;
		}

		if(identifier == null)
		{
			// this is a release
			if(other.identifier != null)
			{
				// other is rc/ea, i.e. non-GA/non-FCS => this is greater.
				return 1;
			}
			return 0;
		}
		// this is rc/ea, i.e. non-GA/non-FCS.
		if(other.identifier == null)
		{
			// other is a release => other is greater.
			return -1;
		}

		// both this and other are rc/ea, i.e. non-GA/non-FCS.
		// the code below is only an approximation.
		return identifier.compareTo(other.identifier);
	}
}