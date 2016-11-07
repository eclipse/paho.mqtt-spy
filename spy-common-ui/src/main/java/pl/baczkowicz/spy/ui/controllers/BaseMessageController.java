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
package pl.baczkowicz.spy.ui.controllers;

import java.util.List;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.controls.StyledTextAreaWrapper;
import pl.baczkowicz.spy.ui.controls.TextAreaInterface;
import pl.baczkowicz.spy.ui.controls.TextAreaWrapper;
import pl.baczkowicz.spy.ui.events.MessageFormatChangeEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexChangeEvent;
import pl.baczkowicz.spy.ui.search.SearchOptions;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Controller for displaying a message.
 */
public abstract class BaseMessageController<T extends FormattedMessage>
{
	final static Logger logger = LoggerFactory.getLogger(BaseMessageController.class);

	@FXML
	protected AnchorPane parentPane;
	
	@FXML
	protected TextArea dataField;
	
	protected TextAreaInterface dataFieldInteface;
	
	protected StyleClassedTextArea styledDataField;


	@FXML
	protected TextField topicField;

	@FXML
	protected TextField timeField;

	@FXML
	protected Label dataLabel;
	
	@FXML
	protected Label lengthLabel;

	protected BasicMessageStoreWithSummary<T> store;
	
	protected T message;
	
	protected Tooltip lengthTooltip;

	protected SearchOptions searchOptions;

	protected boolean detailedView;

	protected IConfigurationManager configurationManager;
	
	protected FormattingManager formattingManager;

	protected boolean styled;

	protected int offset = 0;
	
	public void init()
	{
		if (styled)
		{		
			styledDataField = new StyleClassedTextArea();
												
			AnchorPane.setBottomAnchor(styledDataField, AnchorPane.getBottomAnchor(dataField) - 1);
			AnchorPane.setLeftAnchor(styledDataField, AnchorPane.getLeftAnchor(dataField) - 1);
			AnchorPane.setTopAnchor(styledDataField, AnchorPane.getTopAnchor(dataField) - 1);
			AnchorPane.setRightAnchor(styledDataField, AnchorPane.getRightAnchor(dataField) - 1);
			parentPane.getChildren().add(styledDataField);
			parentPane.getChildren().remove(dataField);
			
			dataFieldInteface = new StyledTextAreaWrapper(styledDataField);
		}
		else
		{
			dataFieldInteface = new TextAreaWrapper(dataField);
		}
		
		dataFieldInteface.setEditable(false);
		dataFieldInteface.setWrapText(true);
		
		dataLabel.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					formattingManager.formatMessage(message, store.getFormatter());
					final String textToDisplay = message.getPrettyPayload();				
					displayNewText(textToDisplay);
				}				
			}
		});
		
		lengthTooltip = new Tooltip();
		lengthLabel.setTooltip(lengthTooltip);
	}

	protected void updateVisibility()
	{
		if (detailedView)
		{
			// Doing this so the panel doesn't get bigger
			AnchorPane.setRightAnchor(topicField, null);
			topicField.setPrefWidth(100);
			
			// Apply sizing and visibility
			AnchorPane.setRightAnchor(topicField, 342.0);
		}
		else
		{
			AnchorPane.setRightAnchor(topicField, 205.0);		
		}
		
		lengthLabel.setVisible(detailedView);
		
		// TODO: basic perspective
	}
	
	public void setMessageIndexOfset(final int offset)
	{
		this.offset = offset;
	}
	
	public void setViewVisibility(final boolean detailedView)
	{
		this.detailedView = detailedView;
		updateVisibility();
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		updateVisibility();
	}
	
	public void onMessageIndexChange(final MessageIndexChangeEvent event)
	{
		updateMessage(event.getIndex() + offset);
	}
	
	public void onFormatChange(final MessageFormatChangeEvent event)
	{
		showMessageData();		
	}
	
	private void updateMessage(final int messageIndex)
	{
		if (messageIndex > 0 && store.getMessages().size() > 0)
		{
			T message = null; 
		
			// Optimised for showing the latest message
			if (messageIndex == 1)
			{
				synchronized (store)
				{
					message = store.getMessages().get(0);
					populate(message);
				}
			}
			else
			{
				synchronized (store)
				{
					final List<T> messages = store.getMessages();
					
					// Make sure we don't try to re-display a message that is not in the store anymore
					if (messageIndex <= messages.size())
					{
						message = messages.get(messageIndex - 1);
						populate(message);
					}
				}				
			}		
			
			final double opacity = 0.02 * ((messageIndex - 1) % 2);
			
			this.parentPane.setStyle("-fx-background-color: " + StylingUtils.createRGBAString(Color.BLACK, opacity));
		}
		else
		{
			clear();
			this.parentPane.setStyle(null);
		}
	}
	
	public void populate(final T message)
	{
		// Don't populate with the same message object
		if (message != null && !message.equals(this.message))
		{
			this.message = message;
	
			final String payload = new String(message.getPayload());
			logger.trace("Message payload = " + payload);
	
			topicField.setText(message.getTopic());
			//qosField.setText(String.valueOf(message.getQoS()));
			timeField.setText(TimeUtils.DATE_WITH_MILLISECONDS_SDF.format(message.getDate()));
			//retainedField.setSelected(message.isRetained());
			
			// Take the length of the raw byte array
			populateLength(message.getRawPayload().length);			
	
			showMessageData();
		}
	}
	
	protected void populateLength(final long length)
	{
		populatePayloadLength(lengthLabel, lengthTooltip, length);
	}
	
	public static void populatePayloadLength(final Label lengthLabel, final Tooltip lengthTooltip, final long length)
	{
		if (lengthTooltip != null)
		{
			lengthTooltip.setText("Message length = " + length);
		}
		
		if (length < 1000)
		{
			lengthLabel.setText("(" + length + "B)");
		}
		else
		{
			final long lengthInKB = length / 1000;
			
			if (lengthInKB < 1000)
			{
				lengthLabel.setText("(" + lengthInKB + "kB)");
			}
			else
			{
				final long lengthInMB = lengthInKB / 1000;
				lengthLabel.setText("(" + lengthInMB + "MB)");				
			}
		}
	}

	public void clear()
	{
		this.message = null;

		topicField.setText("");
		
		dataFieldInteface.clear();		
		
		timeField.setText("");
		lengthLabel.setText("(0)");
	}

	protected void showMessageData()
	{
		if (message != null)
		{
			String textToDisplay = "";

			// If large message detected
			if (message.getRawPayload().length >= UiProperties.getLargeMessageSize(configurationManager.getUiPropertyFile()))
			{
				if (UiProperties.getLargeMessageHide(configurationManager.getUiPropertyFile()))
				{
					textToDisplay = "[message is too large and has been hidden - double click on 'Data' to display]";
				}
				else
				{
					final int max = UiProperties.getLargeMessageSubstring(configurationManager.getUiPropertyFile()); 
					formattingManager.formatMessage(message, store.getFormatter());
					textToDisplay = message.getPrettyPayload().substring(0, max) 
							+ "... [message truncated to " + max + " characters - double click on 'Data' to display]";
				}
			}
			else
			{
				// logger.debug("Formatting browsed message using {}", store.getFormatter().getName());
				formattingManager.formatMessage(message, store.getFormatter());
				textToDisplay = message.getPrettyPayload();
			}
			
			displayNewText(textToDisplay);
		}
	}
	
	private void displayNewText(final String textToDisplay)
	{
		// Won't refresh the text if it is the same...
		if (!textToDisplay.equals(dataFieldInteface.getText()))
		{
			dataFieldInteface.clear();
			dataFieldInteface.appendText(textToDisplay);
			dataFieldInteface.positionCaret(0);
					
			if (searchOptions != null && styled)
			{
				styledDataField.setStyleClass(0, dataFieldInteface.getText().length(), "messageText");
				if (searchOptions.getSearchValue().length() > 0)				
				{
					final String textToSearch = searchOptions.isMatchCase() ? dataFieldInteface.getText() : dataFieldInteface.getText().toLowerCase();
					
					int pos = textToSearch.indexOf(searchOptions.getSearchValue());
					while (pos >= 0)
					{
						styledDataField.setStyleClass(pos, pos + searchOptions.getSearchValue().length(), "messageTextHighlighted");
						pos = textToSearch.indexOf(searchOptions.getSearchValue(), pos + 1);
					}
				}
			}
		}						
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setSearchOptions(final SearchOptions searchOptions)
	{
		this.searchOptions = searchOptions;
	}
	
	public void setConfingurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	public void setFormattingManager(final FormattingManager formattingManager)
	{
		this.formattingManager = formattingManager;
	}
	
	public void setStore(final BasicMessageStoreWithSummary<T> store)
	{
		this.store = store;
	}

	public void setStyled(final boolean styled)
	{
		this.styled = styled;		
	}
}
