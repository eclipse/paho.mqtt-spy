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
package pl.baczkowicz.mqttspy.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.scripts.InteractiveScriptManager;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.scripts.events.ScriptStateChangeEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;
import pl.baczkowicz.spy.ui.scripts.events.ScriptListChangeEvent;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for publications scripts pane.
 */
public class PublicationScriptsController implements Initializable, TitledPaneController
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(PublicationScriptsController.class);
	
	@FXML
	private TableView<PublicationScriptProperties> scriptTable;
	
    @FXML
    private TableColumn<PublicationScriptProperties, String> nameColumn;

    @FXML
    private TableColumn<PublicationScriptProperties, ScriptTypeEnum> typeColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, Boolean> repeatColumn;
        
    @FXML
    private TableColumn<PublicationScriptProperties, ScriptRunningState> runningStatusColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, String> lastPublishedColumn;
    
    @FXML
    private TableColumn<PublicationScriptProperties, Long> messageCountColumn;
		
	private MqttAsyncConnection connection;

	private InteractiveScriptManager scriptManager;
	
	private IKBus eventBus;

	// private EventManager<FormattedMqttMessage> eventManager;

	private Map<ScriptTypeEnum, ContextMenu> contextMenus = new HashMap<>();

	private TitledPane pane;

	private AnchorPane paneTitle;

	private MenuButton settingsButton;

	private MqttConnectionController connectionController;

	private Label titleLabel;

	private String publicationScriptsDirectory;

	private boolean includeSubdirectories;

	public void initialize(URL location, ResourceBundle resources)
	{
		// Table
		nameColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, String>("name"));
		
		typeColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, ScriptTypeEnum>("type"));
		typeColumn
			.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, ScriptTypeEnum>, TableCell<PublicationScriptProperties, ScriptTypeEnum>>()
		{
			public TableCell<PublicationScriptProperties, ScriptTypeEnum> call(
					TableColumn<PublicationScriptProperties, ScriptTypeEnum> param)
			{
				final TableCell<PublicationScriptProperties, ScriptTypeEnum> cell = new TableCell<PublicationScriptProperties, ScriptTypeEnum>()				
				{
					@Override
					public void updateItem(ScriptTypeEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		repeatColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, Boolean>("repeat"));
		repeatColumn
		.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, Boolean>, TableCell<PublicationScriptProperties, Boolean>>()
		{
			public TableCell<PublicationScriptProperties, Boolean> call(
					TableColumn<PublicationScriptProperties, Boolean> param)
			{
				final CheckBoxTableCell<PublicationScriptProperties, Boolean> cell = new CheckBoxTableCell<PublicationScriptProperties, Boolean>()
				{
					@Override
					public void updateItem(final Boolean checked, boolean empty)
					{
						super.updateItem(checked, empty);
						if (!isEmpty() && checked != null && this.getTableRow() != null && this.getTableRow().getItem() != null)
						{
							final PublicationScriptProperties item = (PublicationScriptProperties) this.getTableRow().getItem();
							
							// Anything but subscription scripts can be repeated
							if (!ScriptTypeEnum.SUBSCRIPTION.equals(item.typeProperty().getValue()))
							{	
								this.setDisable(false);
								if (logger.isTraceEnabled())
								{
									logger.trace("Setting repeat for {} to {}", item.getScript().getName(), checked);
								}
								
								item.setRepeat(checked);
							}
							else
							{
								this.setDisable(true);
							}
						}									
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		messageCountColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, Long>("count"));
		messageCountColumn
		.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, Long>, TableCell<PublicationScriptProperties, Long>>()
		{
			public TableCell<PublicationScriptProperties, Long> call(
					TableColumn<PublicationScriptProperties, Long> param)
			{
				final TableCell<PublicationScriptProperties, Long> cell = new TableCell<PublicationScriptProperties, Long>()
				{
					@Override
					public void updateItem(Long item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		runningStatusColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, ScriptRunningState>("status"));
		runningStatusColumn
			.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, ScriptRunningState>, TableCell<PublicationScriptProperties, ScriptRunningState>>()
		{
			public TableCell<PublicationScriptProperties, ScriptRunningState> call(
					TableColumn<PublicationScriptProperties, ScriptRunningState> param)
			{
				final TableCell<PublicationScriptProperties, ScriptRunningState> cell = new TableCell<PublicationScriptProperties, ScriptRunningState>()				
				{
					@Override
					public void updateItem(ScriptRunningState item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		lastPublishedColumn.setCellValueFactory(new PropertyValueFactory<PublicationScriptProperties, String>("lastPublished"));
		lastPublishedColumn.setCellFactory(new Callback<TableColumn<PublicationScriptProperties, String>, TableCell<PublicationScriptProperties, String>>()
		{
			public TableCell<PublicationScriptProperties, String> call(
					TableColumn<PublicationScriptProperties, String> param)
			{
				final TableCell<PublicationScriptProperties, String> cell = new TableCell<PublicationScriptProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});
		
		scriptTable.setRowFactory(new Callback<TableView<PublicationScriptProperties>, TableRow<PublicationScriptProperties>>()
			{
				public TableRow<PublicationScriptProperties> call(
						TableView<PublicationScriptProperties> tableView)
				{
					final TableRow<PublicationScriptProperties> row = new TableRow<PublicationScriptProperties>()					
					{
						@Override
						protected void updateItem(PublicationScriptProperties item, boolean empty)
						{
							super.updateItem(item, empty);
							if (!isEmpty() && item != null)
							{
								final ContextMenu rowMenu = contextMenus.get(item.typeProperty().getValue());

								this.setContextMenu(rowMenu);
							}
						}
					};
					
					row.setOnMouseClicked(event -> 
					{
				        if (event.getClickCount() == 2 && !row.isEmpty()) 
				        {
				            startScript();
				        }
				    });
	
					return row;
				}
			});		
		
		scriptTable.setPlaceholder(new Label("No scripts found - edit your connection settings or use the context menu to set script directory."));
	}
	
	public void init()
	{
		titleLabel = new Label(pane.getText());
		includeSubdirectories = true;
		
		scriptManager = connection.getScriptManager();
		eventBus.subscribe(this, this::onScriptStateChange, ScriptStateChangeEvent.class, new SimpleRunLaterExecutor());
		
		// Note: subscription scripts don't have context menus because they can't be started/stopped manually - for future, consider enabled/disabled
		contextMenus.put(ScriptTypeEnum.PUBLICATION, createDirectoryTypeScriptTableContextMenu(ScriptTypeEnum.PUBLICATION));		
		contextMenus.put(ScriptTypeEnum.BACKGROUND, createDirectoryTypeScriptTableContextMenu(ScriptTypeEnum.BACKGROUND));
		contextMenus.put(ScriptTypeEnum.SUBSCRIPTION, createDirectoryTypeScriptTableContextMenu(ScriptTypeEnum.SUBSCRIPTION));
		
		scriptTable.setContextMenu(createScriptTableContextMenu());
		
		paneTitle = new AnchorPane();
		settingsButton = MqttViewManager.createTitleButtons(this, paneTitle, connectionController);
		
		publicationScriptsDirectory = InteractiveScriptManager.getScriptDirectoryForConnection(
				connection.getProperties().getConfiguredProperties().getPublicationScripts());		

		refreshList();
		scriptTable.setItems(scriptManager.getObservableScriptList());
		
		scriptTable.getSortOrder().clear();
		scriptTable.getSortOrder().add(nameColumn);
	}

	private void refreshList()
	{
		// Directory-driven
		scriptManager.addScripts(publicationScriptsDirectory, includeSubdirectories, ScriptTypeEnum.PUBLICATION);
		
		// As defined
		scriptManager.addScripts(connection.getProperties().getConfiguredProperties().getBackgroundScript(), ScriptTypeEnum.BACKGROUND);		
		
		// Subscription-based
		scriptManager.addSubscriptionScripts(connection.getProperties().getConfiguredProperties().getSubscription());
		
		// TODO: move this to script manager?
		eventBus.publish(new ScriptListChangeEvent(connection));
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
	
	public void startScript(final PublicationScriptProperties item)
	{
		scriptManager.runScript(item.getScript(), true);
	}
	
	public void stopScript(final File file)
	{
		scriptManager.stopScriptFile(file);
	}
	
	private void startScript()
	{
		final PublicationScriptProperties item = scriptTable.getSelectionModel().getSelectedItem();
		if (item != null 
				&& (item.typeProperty().getValue().equals(ScriptTypeEnum.BACKGROUND) 
						|| item.typeProperty().getValue().equals(ScriptTypeEnum.PUBLICATION)))
		{
			startScript(item);
		}
	}
	
	private ContextMenu createScriptTableContextMenu()
	{
		return new ContextMenu(
				createSetScriptDirectoryMenuItem(),
				createIncludeSubdirectoriesMenuItem(),
				new SeparatorMenuItem(),
				createRefreshListMenuItem());
	}
	
	public ContextMenu createDirectoryTypeScriptTableContextMenu(final ScriptTypeEnum type)
	{
		final ContextMenu contextMenu = new ContextMenu();
	
		if (ScriptTypeEnum.PUBLICATION.equals(type) || ScriptTypeEnum.BACKGROUND.equals(type))
		{
			// Start script
			final MenuItem startScriptItem = new MenuItem("Start");
			startScriptItem.setOnAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent e)
				{
					startScript();
				}
			});
			contextMenu.getItems().add(startScriptItem);
			
			// Stop script
			final MenuItem stopScriptItem = new MenuItem("Stop");
			stopScriptItem.setOnAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent e)
				{
					final PublicationScriptProperties item = scriptTable.getSelectionModel()
							.getSelectedItem();
					if (item != null)
					{
						stopScript(item.getScript().getScriptFile());
					}
				}
			});
			contextMenu.getItems().add(stopScriptItem);
	
			// Separator
			contextMenu.getItems().add(new SeparatorMenuItem());
		}
		
		
		// Copy script location
		final MenuItem copyScriptLocationItem = new MenuItem("Copy script location to clipboard");
		copyScriptLocationItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final PublicationScriptProperties item = scriptTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.getScript().getScriptFile().getAbsolutePath());
				}
			}
		});
		contextMenu.getItems().add(copyScriptLocationItem);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		if (type.equals(ScriptTypeEnum.PUBLICATION))
		{
			// Delete
			final Menu deleteItem = new Menu("Delete");
			contextMenu.getItems().add(deleteItem);
			
			final MenuItem deleteFromListItem = new MenuItem("Delete from list (until next refresh)");
			deleteFromListItem.setOnAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent e)
				{
					final PublicationScriptProperties item = scriptTable.getSelectionModel().getSelectedItem();
					if (item != null)
					{
						scriptManager.removeScript(item);
					}
				}
			});
			deleteItem.getItems().add(deleteFromListItem);
			
			final MenuItem deleteFromDiskItem = new MenuItem("Delete from disk (permanently)");
			deleteFromDiskItem.setOnAction(new EventHandler<ActionEvent>()
			{
				public void handle(ActionEvent e)
				{
					final PublicationScriptProperties item = scriptTable.getSelectionModel().getSelectedItem();
					if (item != null)
					{
						scriptManager.removeScript(item);
						if (!item.getScript().getScriptFile().delete())
						{
							DialogFactory.createWarningDialog("File cannot be deleted", 
									"File \"" + item.getScript().getScriptFile().getAbsolutePath() + "\" couln't be deleted. Try doing it manually.");
						}
					}
					refreshList();
				}
			});
			deleteItem.getItems().add(deleteFromDiskItem);
			
			// Separator
			contextMenu.getItems().add(new SeparatorMenuItem());
		}

		contextMenu.getItems().addAll(	createSetScriptDirectoryMenuItem(),
										createIncludeSubdirectoriesMenuItem(),
										new SeparatorMenuItem(),
										createRefreshListMenuItem());		
		return contextMenu;
	}
	
	private MenuItem createSetScriptDirectoryMenuItem()
	{
		// Set directory
		final MenuItem setDirectoryItem = new MenuItem("Set script directory...");
		setDirectoryItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final File initialDirectory = new File(publicationScriptsDirectory);
				
				final DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Select the script directory");
				chooser.setInitialDirectory(initialDirectory);

				final File selectedDirectory = chooser.showDialog(scriptTable.getScene().getWindow());

				if (selectedDirectory != null)
				{			
					publicationScriptsDirectory = selectedDirectory.getAbsolutePath();
					clearAndRefesh();	
				}
				
			}
		});
		
		return setDirectoryItem;
	}
	
	private void clearAndRefesh()
	{
		// Clear current scripts
		scriptManager.getObservableScriptList().clear();
		scriptManager.getScriptsMap().clear();
		scriptTable.getItems().clear();
				
		refreshList();
		
		scriptTable.getSortOrder().clear();
		scriptTable.getSortOrder().add(nameColumn);
	}
	
	private MenuItem createIncludeSubdirectoriesMenuItem()
	{
		// Refresh list
		final CheckMenuItem includeSubdirectoriesItem = new CheckMenuItem("Include subdirectories (recursive)");
		includeSubdirectoriesItem.setSelected(includeSubdirectories);
		includeSubdirectoriesItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				includeSubdirectories = !includeSubdirectories;
				clearAndRefesh();
			}
		});
		
		return includeSubdirectoriesItem;
	}
	
	private MenuItem createRefreshListMenuItem()
	{
		// Refresh list
		final MenuItem refreshListItem = new MenuItem("Refresh list");
		refreshListItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				refreshList();
			}
		});
		
		return refreshListItem;
	}

	public void onScriptStateChange(final ScriptStateChangeEvent event)
	{
		// TODO: update the context menu - but this requires context menu per row, not type
	}

	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}
	
	@Override
	public void updatePane(PaneVisibilityStatus status)
	{
		if (PaneVisibilityStatus.ATTACHED.equals(status))
		{
			settingsButton.setVisible(true);
		}		
		else
		{
			settingsButton.setVisible(false);
		}
	}

	public void setConnectionController(MqttConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public Label getTitleLabel()
	{
		return titleLabel;
	}
}
