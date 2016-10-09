/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Class responsible for updating control panel statistics.
 */
public class ControlPanelStatsUpdater implements Runnable
{
	/** Milliseconds since first stats recorded. */
	private final static long MILLISECONDS = new Date().getTime() - StatisticsManager.stats.getStartDate().getTime();
	
	/** Days since first stats recorded. */
	private final static long DAYS = MILLISECONDS / (1000 * 60 * 60 * 24);
	
	/** Text saying "in X days" or "" when only 1 day since first recorded stats. */
	private final static String IN_DAYS_PHRASE = DAYS > 1 ? (" in " + DAYS + " days") : "";
	
	/** Text saying "since XXXX-XX-XX", where the X is the date of first recorded stats. */
	private final static String SINCE_PHRASE = " since " + TimeUtils.DATE_SDF.format(StatisticsManager.stats.getStartDate());
	
	/** How many different stat messages there are. */
	private final static int STATS_MESSAGES = 6;
	
	/** How many intervals allow before going to next stats. */
	private final static int GO_NEXT_AFTER_INTERVALS = 10;
	
	/** The one second interval. */
	private final static int ONE_SECOND_INTERVAL =  1000;
		
	/** Flag indicating whether the stats are being played automatically (true) or have been paused (false). */
	private boolean statsPlaying;
	
	/** List of getting involved messages. */
	private final List<String> gettingInvolvedDetails;

	/** The controller of the stats control panel item. */
	private final ControlPanelItemController controlPanelItemController;

	/** Reference to the event bus. */
	private IKBus eventBus;

	/** The button on which stats are displayed. */
	private final Button bigButton;

	/** Counts how many seconds lapsed. */
	private int secondCounter;
	
	/** The index of the current statistics message. */
	private int statMessageIndex;

	private IConfigurationManager configurationManager;

	public ControlPanelStatsUpdater(final ControlPanelItemController controlPanelItemController, final Button bigButton, 
			final IKBus eventBus, final IConfigurationManager configurationManager)
	{
		this.controlPanelItemController = controlPanelItemController;
		this.bigButton = bigButton;
		this.eventBus = eventBus;
		this.configurationManager = configurationManager;
		
		final String appName = configurationManager.getDefaultPropertyFile().getApplicationName();
		
		gettingInvolvedDetails = new ArrayList<String>(Arrays.asList(
				"Finding " + appName + " useful? See how you can make " + appName + " even better", 
				"Like your " + appName + "? See how you can help at", 
				"Using " + appName + " on a regular basis? See how you can help at"));
	}	
	
	/**
	 * Displays the initial state of the statistics control panel item.
	 */
	public void show()
	{
		// Default values
		controlPanelItemController.setTitle("Connect to a server to start seeing processing statistics...");		
		controlPanelItemController.setDetails("");
		controlPanelItemController.setStatus(ItemStatus.STATS);
		
		bigButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				moveToNextStatMessage();
			}
		});

		final List<Node> items  = new ArrayList<Node>();

		// Getting involved details
		final Random r = new Random();
		items.add(new Label(gettingInvolvedDetails.get(r.nextInt(gettingInvolvedDetails.size()))));

		final Hyperlink getInvolved = new Hyperlink();
		getInvolved.setText(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Getting-involved");
		getInvolved.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Getting-involved"));
			}
		});
		items.add(getInvolved);
		
		items.add(new Label(":)"));
		controlPanelItemController.getDetails().getChildren().addAll(items);
		
		statsPlaying = true;
		ControlPanelItemController.setButtonProperties(controlPanelItemController.getButton1(), "pause", true, new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				if (statsPlaying)
				{
					ControlPanelItemController.setButtonProperties(controlPanelItemController.getButton1(), "play", true);
					statsPlaying = false;
				}
				else
				{
					ControlPanelItemController.setButtonProperties(controlPanelItemController.getButton1(), "pause", true);
					statsPlaying = true;
				}
				event.consume();
			}
		});
		
		controlPanelItemController.refresh();
		
		new Thread(this).start();
	}
	
	/**
	 * Moves to the next statistics message.
	 */
	private void moveToNextStatMessage()
	{	
		secondCounter = 0;
		
		statMessageIndex++;
		
		if (statMessageIndex == STATS_MESSAGES)
		{
			statMessageIndex = 0;
		}
		
		// Try to update the stats - if all unavailable, ignore
		int retries = 0;		
		while (!refreshStatsMessage(false) && retries < STATS_MESSAGES)
		{				
			statMessageIndex++;
			
			if (statMessageIndex == STATS_MESSAGES)
			{
				statMessageIndex = 0;
			}
			
			retries++;
		}
	}
	
	/**
	 * Refreshes the stats message.
	 *  
	 * @param updateOnly Flag allowing some values to reach 0 and still be displayed
	 * 
	 * @return True if the stats message has been updated
	 */
	private boolean refreshStatsMessage(final boolean updateOnly)
	{
		final String appName = configurationManager.getDefaultPropertyFile().getApplicationName();
		
		if ((statMessageIndex == 0) && (StatisticsManager.stats.getConnections() > 0))
		{
			controlPanelItemController.setTitle(String.format(
					"Your %s made %s connection" + (StatisticsManager.stats.getConnections() > 1 ? "s" : "") + " to MQTT brokers%s.", appName,
					FormattingUtils.formatNumber(StatisticsManager.stats.getConnections()), IN_DAYS_PHRASE));
			return true;
		}

		else if ((statMessageIndex == 1) && (StatisticsManager.stats.getMessagesPublished() > 1))
		{
			controlPanelItemController.setTitle(String.format(
					"Your %s published %s messages to MQTT brokers.", appName,
					FormattingUtils.formatNumber(StatisticsManager.stats.getMessagesPublished()), IN_DAYS_PHRASE));
			return true;
		}

		else if ((statMessageIndex == 2) && (StatisticsManager.stats.getSubscriptions() > 1))
		{
			controlPanelItemController.setTitle(String.format(
					"Your %s made %s subscriptions to MQTT brokers%s.", appName,
					FormattingUtils.formatNumber(StatisticsManager.stats.getSubscriptions()), IN_DAYS_PHRASE));
			return true;
		}

		else if ((statMessageIndex == 3) && (StatisticsManager.stats.getMessagesReceived() > 1))
		{
			controlPanelItemController.setTitle(String.format(
					"Your %s received %s messages%s.", appName,
					FormattingUtils.formatNumber(StatisticsManager.stats.getMessagesReceived()), SINCE_PHRASE));
			return true;
		}

		else if ((statMessageIndex == 4) && (updateOnly || StatisticsManager.getMessagesPublished() > 1))
		{
			controlPanelItemController.setTitle(String.format(
					"Right now your %s is publishing %s msgs/s.", appName,
					StatisticsManager.getMessagesPublished()));
			return true;
		}

		else if ((statMessageIndex == 5) && (updateOnly || StatisticsManager.getMessagesReceived() > 1))
		{
			controlPanelItemController.setTitle(String.format(
					"Right now your %s is munching through %d msgs/s.", appName,
					StatisticsManager.getMessagesReceived()));
			return true;
		}				
		
		return false;
	}

	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("Control Panel Stats Updater");
		
		secondCounter = 0;
		while (true)
		{
			secondCounter++;
			oneCycleRefresh();
			oneCycleMoveToNext();					
			
			if (ThreadingUtils.sleep(ONE_SECOND_INTERVAL))
			{
				break;
			}			
		}
		
		ThreadingUtils.logThreadEnding();
	}
	
	/**
	 * Refreshes currently displayed information.
	 */
	private void oneCycleRefresh()	
	{
		Platform.runLater(new Runnable()
		{				
			@Override
			public void run()
			{
				refreshStatsMessage(true);	
			}
		});
	}
	
	/**
	 * When due, moves to the next stats message.
	 */
	private void oneCycleMoveToNext()
	{
		if (statsPlaying)
		{
			if (secondCounter == GO_NEXT_AFTER_INTERVALS)
			{
				secondCounter = 0;
				Platform.runLater(new Runnable()
				{						
					@Override
					public void run()
					{
						moveToNextStatMessage();							
					}
				});
			}				
		}
		else
		{
			secondCounter = 0;
		}		
	}
}
