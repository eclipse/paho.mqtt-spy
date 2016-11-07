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
package pl.baczkowicz.spy.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.MessageAddedEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexChangeEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexIncrementEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexToFirstEvent;
import pl.baczkowicz.spy.ui.events.MessageRemovedEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.storage.MessageAuditUtils;
import pl.baczkowicz.spy.ui.utils.TextUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for the message navigation buttons.
 */
public class MessageNavigationController implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(MessageNavigationController.class);

	@FXML
	private Label messageLabel;

	@FXML
	private Label filterStatusLabel;
	
	@FXML
	private CheckBox showLatestBox;

	@FXML
	private ToggleGroup wholeMessageFormat;

	@FXML
	private MenuButton formattingMenuButton;

	@FXML
	private Menu formatterMenu;
	
	@FXML
	private Menu customFormatterMenu;	

	@FXML
	private ToggleGroup selectionFormat;

	@FXML
	private Button moreRecentButton;

	@FXML
	private Button lessRecentButton;

	@FXML
	private Button showFirstButton;

	@FXML
	private Button showLastButton;

	@FXML
	private HBox messageIndexBox; 
	
	private int selectedMessage;

	private BasicMessageStoreWithSummary<? extends FormattedMessage> store; 
	
	private TextField messageIndexValueField;
	
	private Label totalMessagesValueLabel;
	
	private IKBus eventBus;
	
	private int messagesShown = 1;

	private MessageAuditUtils messageAuditUtils;

	public void initialize(URL location, ResourceBundle resources)
	{				
		messageIndexValueField = new TextField();
		messageIndexValueField.setEditable(false);
		messageIndexValueField.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ob, String o, String n)
			{
				// expand the textfield
				messageIndexValueField.setPrefWidth(TextUtils.computeTextWidth(
						messageIndexValueField.getFont(), messageIndexValueField.getText()) + 12);
			}
		});
		
		messageLabel.getStyleClass().add("messageIndex");
		messageLabel.setPadding(new Insets(2, 2, 2, 2));
		
		totalMessagesValueLabel = new Label();
		totalMessagesValueLabel.getStyleClass().add("messageIndex");
		totalMessagesValueLabel.setPadding(new Insets(2, 2, 2, 2));
				
		filterStatusLabel = new Label();
		filterStatusLabel.getStyleClass().add("filterOn");
		filterStatusLabel.setPadding(new Insets(2, 2, 2, 2));
		
		messageIndexValueField.setPadding(new Insets(2, 5, 2, 5));
		messageIndexValueField.getStyleClass().add("messageIndex");
		messageIndexValueField.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle(ScrollEvent event)
			{
				
				switch(event.getTextDeltaYUnits()) 
				{
			        case LINES:
			        	updateMessageIndexFromScroll((int) event.getTextDeltaY());
			            break;
			        case PAGES:
			        	updateMessageIndexFromScroll((int) event.getTextDeltaY());
			            break;
			        case NONE:
			        	updateMessageIndexFromScroll((int) event.getDeltaY());			        	
			            break;
				}
			}
		});
		messageIndexValueField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() 
		{
	        @Override
	        public void handle(KeyEvent keyEvent) 
	        {
	        	switch (keyEvent.getCode())
	        	{
		        	case SPACE:
		        	{
		        		showLatestBox.setSelected(!showLatestBox.isSelected());
		        		break;
		        	}
		        	case HOME:
		        	{
		        		showFirst();
		        		break;
		        	}
		        	case END:
		        	{
		        		showLast();
		        		break;
		        	}
		        	case PAGE_UP:
		        	{
		        		changeSelectedMessageIndexDelta(5);
		        		break;
		        	}
		        	case PAGE_DOWN:
		        	{
		        		changeSelectedMessageIndexDelta(-5);
		        		break;
		        	}
		        	case UP:
		        	{
		        		changeSelectedMessageIndexDelta(1);
		        		break;
		        	}
		        	case DOWN:
		        	{
		        		changeSelectedMessageIndexDelta(-1);
		        		break;
		        	}
		        	default:
		        		break;
	        	}
	        }
	    });		
	}
	
	public void updateRange(final int newValue)
	{
		messagesShown = newValue;
		
		updateIndex(false);
	}

	public void init()
	{
		moreRecentButton.setTooltip(new Tooltip("Show more recent message"));
		lessRecentButton.setTooltip(new Tooltip("Show less recent message"));
		showFirstButton.setTooltip(new Tooltip("Show the latest message"));
		showLastButton.setTooltip(new Tooltip("Show the oldest message"));	
	}
	
	// ===================
	// === FXML methods ==
	// ===================
	
	@FXML
	private void showFirst()
	{
		showFirstMessage();
	}

	@FXML
	private void showLast()
	{
		showLastMessage();
	}	

	@FXML
	private void showMoreRecent()
	{
		changeSelectedMessageIndexDelta(-1);
	}	
	
	@FXML
	private void showLessRecent()
	{
		changeSelectedMessageIndexDelta(1);
	}

	// ====================
	// === Other methods ==
	// ====================
		
	public void onMessageAdded(final MessageAddedEvent<FormattedMessage> event)
	{
		// This is registered for filtered messages only
		if (showLatest())
		{
			onNavigateToFirst(new MessageIndexToFirstEvent(this));
		}
		else
		{
			onMessageIndexIncrement(new MessageIndexIncrementEvent(event.getMessages().size(), store));
		}
	}
		
	public void onMessageIndexChange(final MessageIndexChangeEvent event)
	{
		// Make sure this is not from itself
		if (event.getDispatcher() == this)
		{
			return;
		}
		
		// logger.info("{} Index change = " + newSelectedMessage, store.getName()); 
		if (selectedMessage != event.getIndex())
		{
			changeSelectedMessageIndexAbsolute(event.getIndex());
		}		
	}
	
	public void onNavigateToFirst(final MessageIndexToFirstEvent event)
	{
		// logger.info("{} Index change to first", store.getName());
		showFirstMessage();				
	}
	
	public void onMessageIndexIncrement(final MessageIndexIncrementEvent event)
	{
		// logger.info("{} Index increment", store.getName());
		
		selectedMessage = selectedMessage + event.getIncrement();
		
		// Because this is an event saying a new message is available, but we don't want to display it,
		// so by not refreshing the content of the old one we allow uninterrupted interaction with UI fields (e.g. selection, etc.)
		updateIndex(false);			
	}
	
	// TODO: optimise message handling
	public void onMessageRemoved(final MessageRemovedEvent<FormattedMessage> event)
	{
		for (final BrowseRemovedMessageEvent<FormattedMessage> message : event.getMessages())
		{
			if (message.getMessageIndex() < selectedMessage)
			{
				selectedMessage--;					
			}	
		}
		
		updateIndex(false);
	}
	
	private void showFirstMessage()
	{
		if (store.getMessages().size() > 0)
		{
			selectedMessage = 1;
			updateIndex();
		}
		else
		{
			selectedMessage = 0;
			updateIndex();
		}
	}
	
	private int getMaxMessageIndex()
	{
		return store.getMessages().size() - messagesShown + 1;
	}

	private void showLastMessage()
	{
		if (getMaxMessageIndex() > 0)
		{
			selectedMessage = getMaxMessageIndex();			
			updateIndex();
		}
	}
	
	private void changeSelectedMessageIndexDelta(final int count)
	{	
		changeSelectedMessageIndexAbsolute(selectedMessage + count);
	}
	
	private void changeSelectedMessageIndexAbsolute(final int absolute)
	{
		if (getMaxMessageIndex() > 0)
		{
			if (absolute <= 1)
			{
				showFirstMessage();
			}
			else if (absolute >= getMaxMessageIndex())
			{
				showLastMessage();
			}
			else
			{
				selectedMessage = absolute;
				updateIndex();
			}
		}		
	}

	private void updateIndex()
	{
		updateIndex(true);
	}
	
	private void updateIndex(final boolean refreshMessageDetails)
	{
		String selectedIndexValue = "-";
		
		if (selectedMessage > 0)
		{
			if (messagesShown == 1)
			{
				selectedIndexValue = String.valueOf(selectedMessage);
			}
			else
			{
				selectedIndexValue = String.valueOf(selectedMessage) + "-" + String.valueOf(selectedMessage + messagesShown - 1); 
			}
		}
		
		final String totalMessagesValue = "/ " + store.getMessages().size(); 		
		
		if (messageIndexBox.getChildren().size() == 1)
		{
			messageLabel.setText("Message ");	
			messageIndexBox.getChildren().add(messageIndexValueField);
			messageIndexBox.getChildren().add(totalMessagesValueLabel);		
			messageIndexBox.getChildren().add(filterStatusLabel);
		}
		
		messageIndexValueField.setText(selectedIndexValue);		
		totalMessagesValueLabel.setText(totalMessagesValue);

		updateFilterStatus();
		
		if (refreshMessageDetails)
		{
			eventBus.publish(new MessageIndexChangeEvent(selectedMessage, store, this));
		}
	}
	
	private void updateFilterStatus()
	{
		if (!store.browsingFiltersEnabled())
		{			
			if (!store.messageFiltersEnabled())
			{
				filterStatusLabel.setText("");
			}
			else
			{
				filterStatusLabel.setText("(filter is active)");	
			}
		}
		else if (store instanceof ManagedMessageStoreWithFiltering)
		{	
			if (!store.messageFiltersEnabled())
			{
				filterStatusLabel.setText("(" + getBrowsingTopicsInfo((ManagedMessageStoreWithFiltering<?>) store) + ")");
			}
			else
			{
				filterStatusLabel.setText("(" + getBrowsingTopicsInfo((ManagedMessageStoreWithFiltering<?>) store) + "; filter is active)");
			}
		}
	}

	public static String getBrowsingTopicsInfo(final ManagedMessageStoreWithFiltering<?> store)
	{
		final int selectedTopics = store.getFilteredMessageStore().getBrowsedTopics().size();
		final int totalTopics = store.getAllTopics().size();
		
		return "browsing " + selectedTopics + "/" + totalTopics + " " + (totalTopics == 1? "topic" : "topics");		
	}
	
	private void updateMessageIndexFromScroll(final int scroll)
	{
		if (scroll > 0)
    	{
    		changeSelectedMessageIndexDelta(1);
    	}
    	else
    	{
    		changeSelectedMessageIndexDelta(-1);
    	}
	}	

	public void clear()
	{
		messageLabel.setText("No messages");
		messageIndexBox.getChildren().clear();
		messageIndexBox.getChildren().add(messageLabel);
	}
	
	public boolean showLatest()
	{
		return showLatestBox.isSelected();
	}
	
	public void hideShowLatest()
	{
		showLatestBox.setVisible(false);
	}
	
	public void copyMessageToClipboard()
	{
		if (getSelectedMessageIndex() > 0)
		{
			UiUtils.copyToClipboard(messageAuditUtils.getCurrentMessageAsMessageLog(store, getSelectedMessageIndex() - 1));
		}
	}
	
	public void copyMessagesToClipboard()	
	{
		UiUtils.copyToClipboard(messageAuditUtils.getAllMessagesAsMessageLog(store));
	}
	

	public void copyMessageTopicToClipboard()
	{
		if (getSelectedMessageIndex() > 0)
		{
			final FormattedMessage message = store.getMessages().get(getSelectedMessageIndex() - 1);
			UiUtils.copyToClipboard(message.getTopic());
		}
	}
	
	public void copyMessageToFile()
	{
		if (getSelectedMessageIndex() > 0)
		{
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select message audit log file to save to");
			String extensions = "messages";
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message audit log file", extensions));
	
			final File selectedFile = fileChooser.showSaveDialog(getParentWindow());
	
			if (selectedFile != null)
			{
				FileUtils.writeToFile(selectedFile, messageAuditUtils.getCurrentMessageAsMessageLog(store, getSelectedMessageIndex() - 1));
			}
		}
	}
	
	public void copyMessageToBinaryFile()
	{
		if (getSelectedMessageIndex() > 0)
		{
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select file to save to");
			String extensions = "*";
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("File", extensions));
	
			final File selectedFile = fileChooser.showSaveDialog(getParentWindow());
	
			if (selectedFile != null)
			{
				FileUtils.writeToFile(selectedFile, store.getMessages().get(getSelectedMessageIndex() - 1).getRawPayload());
			}
		}
	}
	
	public void copyMessagesToFile()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select message audit log file to save to");
		String extensions = "messages";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message audit log file", extensions));

		final File selectedFile = fileChooser.showSaveDialog(getParentWindow());

		if (selectedFile != null)
		{
			FileUtils.writeToFile(selectedFile, messageAuditUtils.getAllMessagesAsMessageLog(store));
		}
	}

	private Window getParentWindow()
	{
		return messageLabel.getScene().getWindow();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setMessageAuditUtils(final MessageAuditUtils messageAuditUtils)
	{
		this.messageAuditUtils = messageAuditUtils;
	}
	
	public void setStore(final BasicMessageStoreWithSummary<? extends FormattedMessage> store)
	{
		this.store = store;
	}
	
	public int getSelectedMessageIndex()
	{
		return selectedMessage;
	}
	
	public void setViewVisibility(final boolean detailedView)
	{
		// Nothing to do here
	}

	public void toggleDetaileledViewVisibility()
	{
		// Nothing to do here
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
