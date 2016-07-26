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
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.controls.StyledTextAreaWrapper;
import pl.baczkowicz.spy.ui.controls.TextAreaInterface;
import pl.baczkowicz.spy.ui.controls.TextAreaWrapper;
import pl.baczkowicz.spy.ui.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.spy.ui.search.SearchOptions;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Controller for displaying a message.
 */
public class MessageController implements Initializable, MessageIndexChangeObserver, MessageFormatChangeObserver
{
	final static Logger logger = LoggerFactory.getLogger(MessageController.class);

	@FXML
	private AnchorPane parentPane;
	@FXML
	private TextArea dataField;
	
	private TextAreaInterface dataFieldInteface;
	
	private StyleClassedTextArea styledDataField;

	@FXML
	private ToggleButton wrapToggle;
	
	@FXML
	private CheckBox retainedField;

	@FXML
	private TextField topicField;

	@FXML
	private TextField timeField;

	@FXML
	private TextField qosField;

	@FXML
	private Label dataLabel;
	
	@FXML
	private Label lengthLabel;
	
	@FXML
	private Label retainedFieldLabel;
	
	@FXML
	private Label qosFieldLabel;

	private BasicMessageStoreWithSummary<FormattedMqttMessage> store;
	
	private FormattedMqttMessage message;

	private FormatterDetails selectionFormat = null;

	private Tooltip tooltip;
	
	private Tooltip lengthTooltip;

	private SearchOptions searchOptions;

	private boolean detailedView;

	private ConfigurationManager configurationManager;
	
	private FormattingManager formattingManager;

	private boolean styled;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		// All done in init() as this is where the dataFieldInterface is assigned
	}
	
	public void init()
	{
//		dataField.heightProperty().addListener(new ChangeListener<Number>()
//		{
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable,
//					Number oldValue, Number newValue)
//			{
//				logger.info("New height = {}; pane height = {}", newValue, parentPane.getHeight());
//				
//			}
//		});
		
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
		dataFieldInteface.selectedTextProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue)
			{
				updateTooltipText();				
			}
		});		
		
		dataLabel.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					formattingManager.formatMessage(message, store.getFormatter());
					final String textToDisplay = message.getFormattedPayload();				
					displayNewText(textToDisplay);
				}				
			}
		});
		
		tooltip = new Tooltip("");
		tooltip.setWrapText(true);
		
		lengthTooltip = new Tooltip();
		lengthLabel.setTooltip(lengthTooltip);
	}

	private void updateVisibility()
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
		
		qosField.setVisible(detailedView);
		retainedField.setVisible(detailedView);
		qosFieldLabel.setVisible(detailedView);
		retainedFieldLabel.setVisible(detailedView);
		lengthLabel.setVisible(detailedView);
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		detailedView = visible;
		updateVisibility();
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		updateVisibility();
	}
	
	@Override
	public void onMessageIndexChange(final int index)
	{
		updateMessage(index);
	}
	

	@Override
	public void onFormatChange()
	{
		showMessageData();		
	}
	
	private void updateMessage(final int messageIndex)
	{
		if (messageIndex > 0)
		{
			FormattedMqttMessage message = null; 
		
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
					final List<FormattedMqttMessage> messages = store.getMessages();
					
					// Make sure we don't try to re-display a message that is not in the store anymore
					if (messageIndex <= messages.size())
					{
						message = messages.get(messageIndex - 1);
						populate(message);
					}
				}				
			}			
		}
		else
		{
			clear();
		}
	}

	public void populate(final FormattedMqttMessage message)
	{
		// Don't populate with the same message object
		if (message != null && !message.equals(this.message))
		{
			this.message = message;
	
			final String payload = new String(message.getPayload());
			logger.trace("Message payload = " + payload);
	
			topicField.setText(message.getTopic());
			qosField.setText(String.valueOf(message.getQoS()));
			timeField.setText(TimeUtils.DATE_WITH_MILLISECONDS_SDF.format(message.getDate()));
			retainedField.setSelected(message.isRetained());
			
			// Take the length of the raw byte array
			populateLength(message.getRawMessage().getPayload().length);			
	
			showMessageData();
		}
	}
	
	private void populateLength(final long length)
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
		
		qosField.setText("");
		timeField.setText("");
		lengthLabel.setText("(0)");
		retainedField.setSelected(false);
	}
	
	public void formatSelection(final FormatterDetails messageFormat)
	{
		this.selectionFormat = messageFormat;
		
		if (selectionFormat != null)
		{
			updateTooltipText();
			dataFieldInteface.setTooltip(tooltip);
		}
		else			
		{
			dataFieldInteface.setTooltip(null);
		}
	}

	private void showMessageData()
	{
		if (message != null)
		{
			String textToDisplay = "";

			// If large message detected
			if (message.getRawMessage().getPayload().length >= UiProperties.getLargeMessageSize(configurationManager.getUiPropertyFile()))
			{
				if (UiProperties.getLargeMessageHide(configurationManager.getUiPropertyFile()))
				{
					textToDisplay = "[message is too large and has been hidden - double click on 'Data' to display]";
				}
				else
				{
					final int max = UiProperties.getLargeMessageSubstring(configurationManager.getUiPropertyFile()); 
					formattingManager.formatMessage(message, store.getFormatter());
					textToDisplay = message.getFormattedPayload().substring(0, max) 
							+ "... [message truncated to " + max + " characters - double click on 'Data' to display]";
				}
			}
			else
			{
				formattingManager.formatMessage(message, store.getFormatter());
				textToDisplay = message.getFormattedPayload();
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
			
			updateTooltipText();
		}						
	}
	
	private void updateTooltipText()
	{
		if (selectionFormat != null)
		{
			final String tooltipText = FormattingUtils.checkAndFormatText(selectionFormat, dataFieldInteface.getSelectedText());
			
			if (tooltipText.length() > 0)
			{
				tooltip.setText(tooltipText);
			}
			else
			{
				tooltip.setText("[select text to convert]");
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
	
	public void setConfingurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	public void setFormattingManager(final FormattingManager formattingManager)
	{
		this.formattingManager = formattingManager;
	}
	
	public void setStore(final BasicMessageStoreWithSummary<FormattedMqttMessage> store)
	{
		this.store = store;
	}

	public void setStyled(final boolean styled)
	{
		this.styled = styled;		
	}
}
