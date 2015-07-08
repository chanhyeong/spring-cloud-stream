/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.streams.adapter;

import org.springframework.cloud.streams.config.ChannelBindingProperties;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 */
public class DefaultChannelLocator implements ChannelLocator {

	private ChannelBindingProperties module;

	public DefaultChannelLocator(ChannelBindingProperties module) {
		this.module = module;
	}

	@Override
	public String locate(String name) {
		String channelName = extractChannelName("input", name, this.module.getInputChannelName());
		if (channelName!=null) {
			return channelName;
		}
		channelName = extractChannelName("output", name, this.module.getOutputChannelName());
		if (channelName!=null) {
			return channelName;
		}
		return null;
	}

	@Override
	public String tap(String name) {
		return !isDefaultOuputChannel(name) ? this.module.getTapChannelName(getPlainChannelName(name))
				: this.module.getTapChannelName();
	}

	private boolean isDefaultOuputChannel(String channelName) {
		if (channelName.contains(":")) {
			String[] tokens = channelName.split(":", 2);
			channelName = tokens[1];
		}
		return channelName.equals(this.module.getOutputChannelName());
	}

	private String extractChannelName(String start, String name, String externalChannelName) {
		if (name.equals(start)) {
			return externalChannelName;
		}
		else if (name.startsWith(start + ".") || name.startsWith(start + "_")) {
			String prefix = "";
			String channelName = name.substring(start.length() + 1);
			if (channelName.contains(":")) {
				String[] tokens = channelName.split(":", 2);
				String type = tokens[0];
				if ("queue".equals(type)) {
					// omit the type for a queue
					if (StringUtils.hasText(tokens[1])) {
						prefix = tokens[1] + ".";
					}
				}
				else {
					prefix = channelName + (channelName.endsWith(":") ? "" : ".");
				}
			}
			else {
				prefix = channelName + ".";
			}
			return prefix + getPlainChannelName(externalChannelName);
		}
		return null;
	}

	private String getPlainChannelName(String name) {
		if (name.contains(":")) {
			name = name.substring(name.indexOf(":") + 1);
		}
		return name;
	}

}
