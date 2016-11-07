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

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.ui.testcases.InteractiveTestCaseManager;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;
import pl.baczkowicz.spy.testcases.TestCaseManager;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.properties.TestCaseProperties;

/**
 * Controller for the test cases execution window.
 */
public class TestCasesExecutionController extends AnchorPane implements Initializable, TitledPaneController
{
	/** Initial and minimal scene/stage width. */	
	public final static int WIDTH = 780;
	
	/** Initial and minimal scene/stage height. */
	public final static int HEIGHT = 550;
		
	final static Logger logger = LoggerFactory.getLogger(TestCasesExecutionController.class);	

	private TitledPane pane;
	
	@FXML
	private TreeTableView<TestCaseProperties> scriptTree;
	
	private TreeItem<TestCaseProperties> root = new TreeItem<>();
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private TestCaseExecutionController testCaseExecutionPaneController;
	
	@FXML
	private TreeTableColumn<TestCaseProperties, String> nameColumn;	
	
	@FXML
	private TreeTableColumn<TestCaseProperties, String> lastRunColumn;
	
	@FXML
	private TreeTableColumn<TestCaseProperties, TestCaseStatus> statusColumn;
	
	@FXML
	private ContextMenu scriptTreeContextMenu;	

	@FXML
	private MenuItem setLocationMenu;

	@FXML
	private MenuItem enqueueAllMenu;

	@FXML
	private MenuItem enqueueSelectedMenu;

	@FXML
	private MenuItem enqueueNotRunMenu;

	@FXML
	private MenuItem enqueueFailedMenu;
	
	@FXML
	private MenuItem clearEnqueuedMenu;
	
	@FXML
	private Label enqueuedLabel;
	
	@FXML
	private Label passesLabel;
	
	@FXML
	private Label failuresLabel;
	
	@FXML
	private Label runLabel;
	
	@FXML
	private Label totalLabel;
	
	@FXML
	private Label skippedLabel;
		
	private String scriptsLocation;
	
	// private EventManager<FormattedMqttMessage> eventManager;
	
	// private IKBus eventBus;
	
	private BaseScriptManagerInterface scriptManager;

	private InteractiveTestCaseManager testCaseManager;

	// private MqttAsyncConnection connection;

	private MenuButton settingsButton;

	// private ConnectionController connectionController;

	private Label titleLabel;
	
	public void initialize(URL location, ResourceBundle resources)
	{			
		// Set location
		setLocationMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				final DirectoryChooser fileChooser = new DirectoryChooser();
				fileChooser.setTitle("Select test cases location");				
				final File selectedFile = fileChooser.showDialog(scriptTree.getScene().getWindow());

				if (selectedFile != null)
				{								
					scriptsLocation = selectedFile.getAbsolutePath();
					
					testCaseManager.loadTestCases(scriptsLocation);
					root.getChildren().clear();
					
					for (final TestCaseProperties properties : testCaseManager.getTestCasesProperties())
					{
						root.getChildren().add(new TreeItem<TestCaseProperties>(properties));
					}				
					
					scriptTree.getSelectionModel().clearSelection();
					refreshInfo();

					scriptTree.getSortOrder().clear();
					scriptTree.getSortOrder().add(nameColumn);
					// TODO: get all dirs and subdirs?
				}
			}
		});
		
		enqueueAllMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				testCaseManager.enqueueAllTestCases();
				// TODO: start
				final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
			
				if (selected != null && selected.getValue() != null)
				{
					final TestCaseProperties testCaseProperties = selected.getValue();
					
					testCaseManager.runTestCase(testCaseProperties);
					refreshInfo();
				}
			}
		});		
		
		enqueueSelectedMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
			
				if (selected != null && selected.getValue() != null)
				{
					final TestCaseProperties testCaseProperties = selected.getValue();
					
					testCaseManager.enqueueTestCase(testCaseProperties);
					refreshInfo();
				}
			}
		});
		
		enqueueNotRunMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				testCaseManager.enqueueAllNotRun();
				refreshInfo();
			}
		});
		
		enqueueFailedMenu.setOnAction(new EventHandler<ActionEvent>()
		{		
			@Override
			public void handle(ActionEvent event)
			{
				testCaseManager.enqueueAllFailed();
				refreshInfo();
			}
		});
		
		clearEnqueuedMenu.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				testCaseManager.clearEnqueued();	
				refreshInfo();
			}
		});
						
		scriptTree.setRoot(root);
		scriptTree.setShowRoot(false);
		
		scriptTree.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				showSelected();			
				refreshInfo();
			}
		});
		
		nameColumn.setCellValueFactory
		(
	            (TreeTableColumn.CellDataFeatures<TestCaseProperties, String> param) -> 
	            new ReadOnlyStringWrapper(param.getValue().getValue().getName())
	    );
		
		lastRunColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TestCaseProperties, String>, ObservableValue<String>>() 
		{
            @Override public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<TestCaseProperties, String> p) 
            {
                return p.getValue().getValue().lastUpdatedProperty();
            }
        });
		lastRunColumn.setCellFactory(new Callback<TreeTableColumn<TestCaseProperties, String>, TreeTableCell<TestCaseProperties, String>>()
		{
			public TreeTableCell<TestCaseProperties, String> call(TreeTableColumn<TestCaseProperties, String> param)
			{
				final TreeTableCell<TestCaseProperties, String> cell = new TreeTableCell<TestCaseProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						setText(item);
					}
				};
				cell.setAlignment(Pos.CENTER);
				
				return cell;
			}
		});
		
		statusColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus>, ObservableValue<TestCaseStatus>>() 
		{
            @Override public ObservableValue<TestCaseStatus> call(TreeTableColumn.CellDataFeatures<TestCaseProperties, TestCaseStatus> p) 
            {
                return p.getValue().getValue().statusProperty();
            }
        });
		
		statusColumn.setCellFactory(new Callback<TreeTableColumn<TestCaseProperties,TestCaseStatus>, TreeTableCell<TestCaseProperties,TestCaseStatus>>()
		{			
			public TreeTableCell<TestCaseProperties, TestCaseStatus> call(
					TreeTableColumn<TestCaseProperties, TestCaseStatus> param)
			{
				final TreeTableCell<TestCaseProperties, TestCaseStatus> cell = new TreeTableCell<TestCaseProperties, TestCaseStatus>()
				{
					@Override
					public void updateItem(TestCaseStatus item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setGraphic(testCaseExecutionPaneController.getIconForStatus(item));
						}
						else
						{
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.CENTER);
				cell.setPadding(new Insets(0, 0, 0, 0));
				
				return cell;
			}
		});
		
		// Note: important - without that, cell height goes nuts with progress indicator
		scriptTree.setFixedCellSize(24);				
		
		scriptTree.setPlaceholder(new Label("Right click to load test cases..."));
	}	

	public void init()
	{
		titleLabel = new Label(pane.getText());
		
		// scriptManager = new InteractiveScriptManager(eventBus, connection);
		testCaseManager = new InteractiveTestCaseManager(scriptManager, this, testCaseExecutionPaneController);
		
		testCaseExecutionPaneController.setTestCaseManager(testCaseManager);
		
//		if (connectionController != null)
//		{
//			// paneTitle = new AnchorPane();
//			settingsButton = ViewManager.createTitleButtons(this, new AnchorPane(), connectionController);
//		}
	}	
	
	public void refreshInfo()
	{
		totalLabel.setText(totalLabel.getText().substring(0, totalLabel.getText().indexOf(" ") + 1) + testCaseManager.getTotalCount());		
		enqueuedLabel.setText(enqueuedLabel.getText().substring(0, enqueuedLabel.getText().indexOf(" ") + 1) + testCaseManager.getEnqueuedCount());
		int passes = 0;
		int failures = 0;
		int skipped = 0;
		int run = 0;
		
		enqueueAllMenu.setDisable(true);
		enqueueSelectedMenu.setDisable(true);
		enqueueNotRunMenu.setDisable(true);
		enqueueFailedMenu.setDisable(true);
		clearEnqueuedMenu.setDisable(testCaseManager.getEnqueuedCount() == 0 ? true : false);
		
		if (root.getChildren().size() > 0 && testCaseManager.getTotalCount() > 0)
		{		
			enqueueAllMenu.setDisable(false);
			
			final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();

			if (selected != null && selected.getValue() != null)
			{
				enqueueSelectedMenu.setDisable(false);
			}			
			
			for (final TestCaseProperties testCase : testCaseManager.getTestCasesProperties())
			{
				if (TestCaseStatus.PASSED.equals(testCase.statusProperty().getValue()))
				{
					passes++;
				}
				else if (TestCaseStatus.FAILED.equals(testCase.statusProperty().getValue()))
				{
					failures++;
				} 
				else if (TestCaseStatus.SKIPPED.equals(testCase.statusProperty().getValue()))
				{
					skipped++;
				}
				run = passes + failures + skipped;
			}
			
			enqueueNotRunMenu.setDisable(run != testCaseManager.getTotalCount() ? false : true);
			enqueueFailedMenu.setDisable(failures > 0 ? false : true);		
		}
		
		passesLabel.setText(passesLabel.getText().substring(0, passesLabel.getText().indexOf(" ") + 1) + passes);
		failuresLabel.setText(failuresLabel.getText().substring(0, failuresLabel.getText().indexOf(" ") + 1) + failures);		
		skippedLabel.setText(skippedLabel.getText().substring(0, skippedLabel.getText().indexOf(" ") + 1) + skipped);
		runLabel.setText(runLabel.getText().substring(0, runLabel.getText().indexOf(" ") + 1) + run);
		
		if (scriptsLocation != null)
		{
			final String parentDir = scriptsLocation + System.getProperty("file.separator");		
			testCaseManager.exportTestCasesResultsAsCSV(new File(parentDir+ "results_" + TestCaseManager.testCasesFileSdf.format(new Date()) + ".csv"));
		}
	}
	
	public void showSelected()
	{
		final TreeItem<TestCaseProperties> selected = scriptTree.getSelectionModel().getSelectedItem();
 
		if (selected != null && selected.getValue() != null)
		{					
			final TestCaseProperties testCaseProperties = selected.getValue();
			logger.info("About to display selected test case - " + testCaseProperties.getName());
			testCaseExecutionPaneController.display(testCaseProperties, testCaseProperties.getSteps());
		}
		else
		{
			logger.warn("No test case selected");
		}
	}
	
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
//	public void setEventBus(final IKBus eventBus)
//	{
//		this.eventBus = eventBus;
//	}
	
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

//	public void setConnection(MqttAsyncConnection connection)
//	{
//		this.connection = connection;		
//	}
	
//	public void setConnectionController(final ConnectionController connectionController)
//	{
//		this.connectionController = connectionController;
//	}
	
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

	@Override
	public Label getTitleLabel()
	{
		return titleLabel;
	}

	public void setScriptManager(final BaseScriptManagerInterface scriptManager)
	{
		this.scriptManager = scriptManager;		
	}

	public void setSettingsButton(final MenuButton settingsButton)
	{
		this.settingsButton = settingsButton;		
	}
}