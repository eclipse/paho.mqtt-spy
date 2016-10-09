/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.spy.ui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoErrorEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoReceivedEvent;
import pl.baczkowicz.spy.ui.generated.versions.SpyVersions;
import pl.baczkowicz.spy.ui.properties.VersionInfoProperties;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.ImageUtils;
import pl.baczkowicz.spy.ui.versions.VersionManager;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * Controller for the 'about' window.
 */
public class AboutController implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(AboutController.class);
	
	@FXML
	private Label javaLabel;
	
	@FXML
	private Label versionLabel;
	
	@FXML
	private Label projectInfoLabel;
	
	@FXML
	private Label statusLabel;
	
	@FXML
	private ImageView logo;
	
	@FXML
	private ImageView statusIcon;
	
	@FXML
	private ProgressIndicator progressIndicator;

	private VersionManager versionManager;
	
	private IKBus eventBus;

	private PropertyFileLoader propertyFileLoader;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{			
		final String javaVersion = System.getProperty("java.version") + " / " + System.getProperty("java.vendor");
		final String osVersion = System.getProperty("os.name") + " / " + System.getProperty("os.version") + " / " + System.getProperty("os.arch");
		javaLabel.setText("Java: " + javaVersion  + System.getProperty("line.separator") + "OS: " + osVersion);	
		
		// Set up links to the project home page
		projectInfoLabel.setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				openProjectWebsite();				
			}
		});
		projectInfoLabel.setCursor(Cursor.HAND);
		
		statusLabel.setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				openProjectWebsite();				
			}
		});
		statusLabel.setCursor(Cursor.HAND);
		
		logo.setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				openProjectWebsite();				
			}
		});
		logo.setCursor(Cursor.HAND);
		
		// Double click to refresh
		statusIcon.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					reloadVersionInfo();
				}
			}
		});
	}	
	
	public void reloadVersionInfo()
	{
		progressIndicator.setVisible(true);
		statusIcon.setVisible(false);
		statusLabel.setText("Connecting to the mqtt-spy update server...");
		
		if (!versionManager.isLoading())
		{
			// Run the version check in a separate thread, so that it doesn't block JavaFX
			new Thread(new Runnable()
			{			
				@Override
				public void run()
				{
					try
					{
						versionManager.setLoading(true);
						
						// Wait some time for the window to open properly
						ThreadingUtils.sleep(2000);			
						
						final SpyVersions versions = versionManager.loadVersions();
						
						logger.debug("Retrieved version info = " + versions.toString());
						eventBus.publish(new VersionInfoReceivedEvent(versions));
					}
					catch (final XMLException e)
					{
						// If an error occurred					
						eventBus.publish(new VersionInfoErrorEvent(e));			
					}
				}
			}).start();			
		}		
	}
	
	public void init()
	{
		eventBus.subscribe(this, this::onVersionInfoReceived, VersionInfoReceivedEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onVersionInfoError, VersionInfoErrorEvent.class, new SimpleRunLaterExecutor());
		
		versionLabel.setText(propertyFileLoader.getFullVersionName());
		
		reloadVersionInfo();
	}
	
	@FXML
	private void openProjectWebsite()
	{
		eventBus.publish(new ShowExternalWebPageEvent("http://kamilfb.github.io/mqtt-spy/"));				
	}
	
	public void setPropertyFileLoader(final PropertyFileLoader propertyFileLoader)
	{
		this.propertyFileLoader = propertyFileLoader;
	}
	
	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;
	}
	
	public void onVersionInfoReceived(final VersionInfoReceivedEvent event)
	{
		progressIndicator.setVisible(false);
		statusIcon.setVisible(true);	
		
		final VersionInfoProperties properties = versionManager.getVersionInfoProperties(propertyFileLoader);			
		final String imageName = ControlPanelItemController.getStatusIconName(properties.getStatus());	
		statusIcon.setImage(ImageUtils.createIcon(imageName).getImage());
		statusLabel.setText(properties.getTitle() + System.getProperty("line.separator") + properties.getDetails());
	}

	public void onVersionInfoError(final VersionInfoErrorEvent event)
	{
		progressIndicator.setVisible(false);
		statusIcon.setVisible(true);
		statusLabel.setText("Error occurred while getting version info. Please perform manual update.");
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
