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
package pl.baczkowicz.mqttspy.ui.messagelog;

import java.io.File;
import java.util.List;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.LoggedMqttMessage;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogParserUtils;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.controllers.MqttSpyMainController;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * Tasks responsible for reading the message log.
 */
public class LogReaderTask extends TaskWithProgressUpdater<List<BaseMqttMessage>>
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(LogReaderTask.class);
	
	/** The file to read from. */
	private File selectedFile;
	
	/** View manager - used for loading the message log tab. */
	protected MqttViewManager viewManager;
	
	/** Main controller. */
	protected MqttSpyMainController controller;
	
	/**
	 * Creates a LogReaderTask with the supplied parameters.
	 * 
	 * @param selectedFile The file to read from
	 * @param connectionManager The connection manager
	 * @param mainController The main controller
	 */
	public LogReaderTask(final File selectedFile, final MqttViewManager viewManager, final MqttSpyMainController mainController)
	{
		this.selectedFile = selectedFile;
		this.viewManager = viewManager;
		this.controller = mainController;
		super.updateTitle("Processing message audit log file " + selectedFile.getName());
	}

	@Override
	protected List<BaseMqttMessage> call() throws Exception
	{
		try
		{			
			// Read the message log
			updateMessage("Please wait - reading message audit log [1/4]");
			updateProgress(0, 4);
			final List<String> fileContent = FileUtils.readFileAsLines(selectedFile);					
			final long totalItems = fileContent.size();
			updateProgress(totalItems, totalItems * 4);
			
			if (isCancelled())
			{
				logger.info("Task cancelled!");
				return null;
			}
			
			// Parser the message log (string -> LoggedMqttMessage)
			updateMessage("Please wait - parsing " + fileContent.size() + " messages [2/4]");					
			final List<LoggedMqttMessage> loggedMessages = MqttMessageLogParserUtils.parseMessageLog(fileContent, this, totalItems, totalItems * 4);
			updateProgress(totalItems * 2, totalItems * 4);
			
			if (isCancelled())
			{
				logger.info("Task cancelled!");
				return null;
			}
			
			// Process the message log (LoggedMqttMessage -> ReceivedMqttMessage)
			updateMessage("Please wait - processing " + loggedMessages.size() + " messages [3/4]");					
			final List<BaseMqttMessage> processedMessages = MqttMessageLogParserUtils.processMessageLog(loggedMessages, this, totalItems * 2, totalItems * 4);
			updateProgress(totalItems * 3, totalItems * 4);
			
			if (isCancelled())
			{
				logger.info("Task cancelled!");
				return null;
			}
			
			// Display message log
			updateMessage("Please wait - displaying " + loggedMessages.size() + " messages [4/4]");	
			Platform.runLater(new Runnable()
			{							
				@Override
				public void run()
				{
					viewManager.loadMessageLogTab(controller, selectedFile.getName(), processedMessages);								
				}
			});	
			
			// Done!
			updateMessage("Finished!");
			updateProgress(4, 4);
			
			// Make the last message visible for some time
			ThreadingUtils.sleep(500);
			
			return processedMessages;
		}
		catch (Exception e)
		{
			logger.error("Cannot process the message audit log - {}", selectedFile.getName(), e);
		}
		
		return null;
	}
}
