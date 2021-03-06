/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.udp;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class UdpDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {
    public static final String NAME = "UDP Writer";

    private DestinationConnectorProperties destinationConnectorProperties;
    private String address;
    private int port=0;
    private boolean isMessageByteArray;
    public UdpDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties(false);
        port=0;
        address="";
        isMessageByteArray=false;
    }

    public UdpDispatcherProperties(UdpDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }    

    public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
    public String toFormattedString() {
        return "Script Executed";
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new UdpDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    // @formatter:off
    @Override public void migrate3_0_1(DonkeyElement element) {}
    @Override public void migrate3_0_2(DonkeyElement element) {} // @formatter:on

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);
    }

    // @formatter:off
    @Override public void migrate3_2_0(DonkeyElement element) {}
    @Override public void migrate3_3_0(DonkeyElement element) {}
    @Override public void migrate3_4_0(DonkeyElement element) {}
    @Override public void migrate3_5_0(DonkeyElement element) {}
    @Override public void migrate3_6_0(DonkeyElement element) {}
    @Override public void migrate3_7_0(DonkeyElement element) {} // @formatter:on

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());        
        return purgedProperties;
    }

	public String getAddress() {
		return this.address;		
	}
	
	public void setAddress(String address) {
		this.address=address;		
	}

	public void setMessageByteArray(boolean isMessageByteArray) {
		this.isMessageByteArray = isMessageByteArray;
	}

	public boolean isMessageByteArray() {
		return isMessageByteArray;
	}
}
