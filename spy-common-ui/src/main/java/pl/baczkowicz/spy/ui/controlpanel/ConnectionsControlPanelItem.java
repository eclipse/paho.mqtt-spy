/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.spy.ui.controlpanel;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.IConnectionViewManager;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.events.ShowEditConnectionsWindowEvent;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public class ConnectionsControlPanelItem implements IControlPanelItem
{
	private final static Logger logger = LoggerFactory.getLogger(ConnectionsControlPanelItem.class);

	private static final double MAX_CONNECTIONS_HEIGHT = 350;
	
	private IConfigurationManager configurationManager;

	private IKBus eventBus;

	private IConnectionViewManager connectionManager;

	private IConnectionFactory connectionFactory;

	public ConnectionsControlPanelItem(final IConfigurationManager configurationManager, 
			final IConnectionViewManager connectionManager, final IConnectionFactory connectionFactory, final IKBus eventBus)
	{
		this.configurationManager = configurationManager;
		this.eventBus = eventBus;
		this.connectionManager = connectionManager;
		this.connectionFactory = connectionFactory;
	}

	@Override
	public void update(ControlPanelItemController controlPanelItemController, Button button)
	{
		showConnections(controlPanelItemController, button, configurationManager, connectionManager, eventBus);		
	}
	
	@SuppressWarnings("unchecked")
	public void showConnections(final ControlPanelItemController controller, final Button button, 
			final IConfigurationManager configurationManager, final IConnectionViewManager connectionManager, final IKBus eventBus)
	{
		button.setMaxHeight(MAX_CONNECTIONS_HEIGHT);
		
		// Clear any previously displayed connections
		while (controller.getCustomItems().getChildren().size() > 2) { controller.getCustomItems().getChildren().remove(2); }
		
		final int connectionCount = configurationManager.getConnections().size();
		if (connectionCount > 0)
		{
			controller.setTitle("You have " + connectionCount + " " + "connection" + (connectionCount > 1 ? "s" : "") + " configured.");
			controller.setDetails("Click here to edit your connections or on the relevant button to open, connect, reconnect or disconnect.");
			controller.setStatus(ItemStatus.OK);
			
			List<ConfiguredConnectionGroupDetails> groups = configurationManager.getOrderedGroups();		
			List<Label> labels = new ArrayList<>();
			for (final ConfiguredConnectionGroupDetails group : groups)
			{
				final List<ModifiableConnection> connections = (List<ModifiableConnection>) configurationManager.getConnections(group);
				if (connections.isEmpty())
				{
					continue;
				}
				
				FlowPane buttons = new FlowPane();
				buttons.setVgap(4);
				buttons.setHgap(4);
				buttons.setMaxHeight(Double.MAX_VALUE);
				
				if (groups.size() > 1)
				{
					final Label groupLabel = new Label(group.getFullName() + " : ");
					
					// Do some basic alignment
					groupLabel.widthProperty().addListener(new ChangeListener<Number>()
					{
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
						{
							double maxWidth = 0;
							for (final Label label : labels)
							{
								if (maxWidth < label.getWidth())
								{
									maxWidth = label.getWidth();
								}
							}
							for (final Label label : labels)
							{
								logger.trace("Setting min width for " + label.getText() + " to " + maxWidth);
								label.setMinWidth(maxWidth);
							}							
						}
					});
					labels.add(groupLabel);
					buttons.getChildren().add(groupLabel);
				}
				
				for (final ModifiableConnection connection : connections)
				{
					buttons.getChildren().add(connectionFactory.createConnectionButton(connection));
				}
				
				controller.getCustomItems().getChildren().add(buttons);
				
				button.setOnAction(new EventHandler<ActionEvent>()
				{			
					@Override
					public void handle(ActionEvent event)
					{
						eventBus.publish(new ShowEditConnectionsWindowEvent(button.getScene().getWindow(), false, null));
					}
				});
			}
		}
		else
		{
			controller.setTitle("You haven't got any connections configured.");
			controller.setDetails("Click here to create a new connection...");
			controller.setStatus(ItemStatus.INFO);
			
			button.setOnAction(new EventHandler<ActionEvent>()
			{			
				@Override
				public void handle(ActionEvent event)
				{
					eventBus.publish(new ShowEditConnectionsWindowEvent(button.getScene().getWindow(), true, null));
				}
			});
		}
		controller.refresh();
	}
}
