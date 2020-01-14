/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class UdpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private Logger scriptLogger = Logger.getLogger("udp-connector");
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private UdpDispatcherProperties connectorProperties;
    private String scriptId;
    private DatagramSocket socket;
    private InetAddress  address;
 
    private byte[] buf;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (UdpDispatcherProperties) getConnectorProperties();       
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public void onStart() throws ConnectorTaskException {
    	
        try {
			socket = new DatagramSocket();
			address = InetAddress.getByName("localhost");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }

    @Override
    public void onStop() throws ConnectorTaskException {
    	socket.close();
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
    	socket.close();    	
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {}

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage msg) throws InterruptedException {
        UdpDispatcherProperties javaScriptDispatcherProperties = (UdpDispatcherProperties) connectorProperties;

        try {         
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING));
            buf = msg.getEncoded().getContent().getBytes();
            DatagramPacket packet 
              = new DatagramPacket(buf, buf.length, address, 4445);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(
              packet.getData(), 0, packet.getLength());           
            
            //TODO:Execute UDP package send
            Response response = new Response(received);
            response.setValidate(javaScriptDispatcherProperties.getDestinationConnectorProperties().isValidateResponse());

            return response;
        } catch (Exception e) {
            logger.error("Error executing script (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), msg.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error executing script", e));
            return new Response(Status.ERROR, null, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error executing script", e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }
    }

 
}
